package com.vena.usageservice.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.vena.kafka.event.AlertingEvent;
import com.vena.kafka.event.EnergyUsageEvent;
import com.vena.usageservice.client.DeviceClient;
import com.vena.usageservice.client.UserClient;
import com.vena.usageservice.dto.DeviceDto;
import com.vena.usageservice.dto.UsageDto;
import com.vena.usageservice.dto.UserDto;
import com.vena.usageservice.modal.Device;
import com.vena.usageservice.modal.DeviceEnergy;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsageService {

    // Inject InfluxDBClient via constructor
    private final InfluxDBClient influxDBClient;
    private final DeviceClient deviceClient;
    private final UserClient userClient;

    private final KafkaTemplate<String, AlertingEvent> kafkaTemplate;

    @Value("${influx.bucket}")
    private String bucket;

    @Value("${influx.org}")
    private String orgName;

    @KafkaListener(topics = "energy-usage", groupId = "usage-service")
    public void energyUsageEvent(EnergyUsageEvent energyUsageEvent) {
        log.debug("Received energy usage event: {}", energyUsageEvent);
        // Write the energy usage event to InfluxDB
        Point point = Point.measurement("energy_usage")
                .addTag("deviceId", String.valueOf(energyUsageEvent.deviceId()))
                .addField("energyConsumed", energyUsageEvent.energyConsumed())
                .time(energyUsageEvent.timestamp(), WritePrecision.MS);
        influxDBClient.getWriteApiBlocking().writePoint(bucket, orgName, point);
        log.info("Received energy usage event: {}", energyUsageEvent);
        log.debug("Point written to InfluxDB for deviceId={} energy={}", energyUsageEvent.deviceId(), energyUsageEvent.energyConsumed());
    }

    @Scheduled(cron = "*/10 * * * * *")
    public void aggregateDeviceEnergyUsage() {
        final Instant now = Instant.now();
        log.debug("aggregateDeviceEnergyUsage triggered at {}", now);
        List<FluxTable> tables = getFluxTables(now);

        List<DeviceEnergy> deviceEnergies = new ArrayList<>();

        for (FluxTable table : tables) {
            table.getRecords().forEach(record -> {
                long deviceId = Long.parseLong(
                        Objects.requireNonNull(record.getValueByKey("deviceId")).toString()
                );

                double energyConsumed = record.getValueByKey("_value") instanceof Number
                        ? ((Number) Objects.requireNonNull(record.getValueByKey("_value"))).doubleValue()
                        : 0.0;

                deviceEnergies.add(new DeviceEnergy(deviceId, energyConsumed, null));
            });
        }

        log.info("Device Energy: {}", deviceEnergies);

        for (DeviceEnergy deviceEnergy : deviceEnergies) {
            try {
                final DeviceDto deviceResponse = deviceClient.getDeviceById(deviceEnergy.getDeviceId());

                if (deviceResponse == null || deviceResponse.id() == null) {
                    log.error("Device not found for ID: {}", deviceEnergy.getDeviceId());
                    continue;
                }

                deviceEnergy.setUserId(deviceResponse.userId());
            } catch (Exception e) {
                log.error("Error fetching device details for device ID: {}", deviceEnergy.getDeviceId(), e);
            }
        }

        deviceEnergies.removeIf(deviceEnergy -> deviceEnergy.getUserId() == null);

        Map<Long, List<DeviceEnergy>> userDeviceEnergyMap =
                deviceEnergies.stream()
                        .collect(Collectors.groupingBy(DeviceEnergy::getUserId));

        log.info("User-Device Energy Map: {}", userDeviceEnergyMap);

        List<Long> userIds = new ArrayList<>(userDeviceEnergyMap.keySet());
        final Map<Long, Double> userThresholdMap = new HashMap<>();
        final Map<Long, String> userEmailMap = new HashMap<>();
        final Map<Long, Boolean> userAlertingMap = new HashMap<>();

        for (final Long userId : userIds) {
            try{
                UserDto user = userClient.getUserById(userId);
                if (user != null) {
                    userThresholdMap.put(userId, user.energyAlertingThreshold());
                    userEmailMap.put(userId, user.email());
                    userAlertingMap.put(userId, user.alerting());
                }
            } catch (Exception e) {
                log.error("Error fetching user details for user ID: {}", userId, e);
            }
        }

        final List<Long> alertedUsers = new ArrayList<>(userThresholdMap.keySet());
        for (final Long userId : alertedUsers) {
            final Double threshold = userThresholdMap.get(userId);
            final List<DeviceEnergy> devices = userDeviceEnergyMap.getOrDefault(userId, Collections.emptyList());

            final Double totalConsumption = devices.stream()
                    .mapToDouble(DeviceEnergy::getEnergyConsumed)
                    .sum();
            
            final Boolean alertingEnabled = userAlertingMap.getOrDefault(userId, false);
            
            if (threshold == null) {
                log.warn("User {} has no energy threshold", userId);
                continue;
            }
            if (alertingEnabled && totalConsumption > threshold) {
                log.info("User {} exceeded energy threshold of {} kWh. Total consumption: {} kWh", userId, threshold, totalConsumption);
                // produce kafka message for alert service
                final AlertingEvent alertingEvent = AlertingEvent.builder()
                        .userId(userId)
                        .message("Energy consumption threshold exceeded")
                        .threshold(threshold)
                        .energyConsumed(totalConsumption)
                        .email(userEmailMap.get(userId))
                        .build();

                log.debug("Sending alert event to Kafka for user {}: {}", userId, alertingEvent);
                kafkaTemplate.send("energy-alerts", alertingEvent).whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send alert message for user {}: {}", userId, ex.getMessage(), ex);
                    } else {
                        if (result != null && result.getRecordMetadata() != null) {
                            log.info("Alert message sent for user {} to topic {} partition {} offset {}",
                                    userId,
                                    result.getRecordMetadata().topic(),
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset());
                        } else {
                            log.info("Alert message sent for user {} (metadata not available)", userId);
                        }
                    }
                });
            } else {
                log.info("User {} energy consumption is within threshold. Total consumption: {} kWh", userId, totalConsumption);
            }
        }
    }

    private @NonNull List<FluxTable> getFluxTables(Instant now) {
        final Instant oneHourAgo = now.minusSeconds(3600);

        String influxQuery = String.format("""
        from(bucket: "%s")
          |> range(start: time(v: "%s"), stop: time(v: "%s"))
          |> filter(fn: (r) => r._measurement == "energy_usage")
          |> filter(fn: (r) => r._field == "energyConsumed")
          |> group(columns: ["deviceId"])
          |> sum(column: "_value")
        """, bucket, oneHourAgo, now);

        QueryApi queryApi = influxDBClient.getQueryApi();
        return queryApi.query(influxQuery, orgName);
    }

    public UsageDto getXDaysUsageForUser(Long userId, int days) {
        log.info("Fetching usage data for userId={} over the last {} days", userId, days);
        if (days <= 0) {
            throw new IllegalArgumentException("days must be positive");
        }
        final List<DeviceDto> deviceDto = deviceClient.getAllDevicesForUser(userId);

        final List<Device> devices = new ArrayList<>();
        for (DeviceDto devicesDto : deviceDto) {
            devices.add(Device.builder()
                    .id(devicesDto.id())
                    .name(devicesDto.name())
                    .type(devicesDto.type())
                    .location(devicesDto.location())
                    .userId(devicesDto.userId())
                    .build());
        }


        if (devices == null || devices.isEmpty()) {
            return UsageDto.builder().userId(userId)
                    .devices(Collections.emptyList())
                    .build();
        }

        List<String> deviceIdStrings = devices.stream()
                .map(Device::getId)
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .toList();

        final Instant now = Instant.now();
        final Instant start = now.minusSeconds((long) days * 24 * 3600);

        // flux db query things
        final String deviceFilter = deviceIdStrings.stream()
                .map(idStr -> String.format("r[\"deviceId\"] == \"%s\"", idStr))
                .collect(Collectors.joining(" or "));

        String fluxQuery = String.format("""
                from(bucket: "%s")
                  |> range(start: time(v: "%s"), stop: time(v: "%s"))
                  |> filter(fn: (r) => r._measurement == "energy_usage")
                  |> filter(fn: (r) => r._field == "energyConsumed")
                  |> filter(fn: (r) => %s)
                  |> group(columns: ["deviceId"])
                  |> sum(column: "_value")
                """, bucket, start.toString(), now.toString(), deviceFilter);

        final Map<Long, Double> aggregatedMap = new HashMap<>();

        try {
            QueryApi queryApi = influxDBClient.getQueryApi();
            List<FluxTable> tables = queryApi.query(fluxQuery, orgName);

            for (FluxTable table : tables) {
                for (FluxRecord record : table.getRecords()) {
                    Object deviceIdObj = record.getValueByKey("deviceId");
                    String deviceIdStr = deviceIdObj == null ? null : deviceIdObj.toString();
                    if (deviceIdStr == null) continue;

                    Object value = record.getValueByKey("_value");
                    double energyConsumed = value instanceof Number number ? number.doubleValue() : 0.0;

                    try {
                        Long deviceId = Long.valueOf(deviceIdStr);
                        aggregatedMap.put(deviceId, aggregatedMap.getOrDefault(deviceId, 0.0) + energyConsumed);
                    } catch (NumberFormatException e) {
                        log.warn("Invalid deviceId format: {}", deviceIdStr);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error querying InfluxDB for userId={} over the last {} days", userId, days, e);
            devices.forEach(device -> device.setEnergyConsumed(0.0));
            return UsageDto.builder()
                    .userId(userId)
                    .devices(Collections.emptyList())
                    .build();
        }
        // populate devices with aggregated energy consumption
        for (Device device : devices) {
            if (device == null || device.getId() == null) continue;
            device.setEnergyConsumed(aggregatedMap.getOrDefault(device.getId(), 0.0));
        }
        log.info("Aggregated usage data for userId={} over the last {} days: {}", userId, days, aggregatedMap);

        final List<DeviceDto> deviceDtos = devices.stream()
                .map(device -> DeviceDto.builder()
                        .id(device.getId())
                        .name(device.getName())
                        .type(device.getType())
                        .location(device.getLocation())
                        .userId(device.getUserId())
                        .energyConsumed(device.getEnergyConsumed())
                        .build())
                .toList();
        return UsageDto.builder()
                .userId(userId)
                .devices(deviceDtos)
                .build();
    }
}
