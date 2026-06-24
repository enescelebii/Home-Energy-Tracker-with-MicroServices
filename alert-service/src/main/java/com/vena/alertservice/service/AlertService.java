package com.vena.alertservice.service;


import com.vena.alertservice.client.DeviceClient;
import com.vena.alertservice.client.UserClient;
import com.vena.alertservice.dto.DeviceDto;
import com.vena.alertservice.dto.UserDto;
import com.vena.kafka.event.AlertingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlertService {

    private final EmailService emailService;
    private final UserClient userClient;
    private final DeviceClient deviceClient;


    @KafkaListener(topics = "energy-alerts", groupId = "alert-service-group")
    public void energyUsageAlertEvent(AlertingEvent event) {
        log.info("Received energy usage alert event: {}", event);

        final List<DeviceDto> devices = deviceClient.getDevicesById(event.getUserId());


        final String subject = "Energy Usage Alert For User: " + event.getUserId();
        final String message = "You have exceeded your energy usage limit." + "\nDevices: " + devices + event.getMessage() + "\nThreshold: " + event.getThreshold() + "\nEnergy Consumed: " + event.getEnergyConsumed();

        emailService.sendEmail(event.getEmail(), subject, message, event.getUserId());

    }

}
