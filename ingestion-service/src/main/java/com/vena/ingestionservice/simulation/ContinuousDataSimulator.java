package com.vena.ingestionservice.simulation;


import com.vena.ingestionservice.dto.EnergyUsageDto;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;


import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Random;

@Component
@RequiredArgsConstructor
@Slf4j
public class ContinuousDataSimulator implements CommandLineRunner {

    private final RestTemplate restTemplate = new RestTemplate();
    private final Random random = new Random();

    @Value("${simulation.request-per-interval}")
    private int requestPerInterval;
    @Value("${simulation.ingestion-endpoint}")
    private String ingestionEnpoint;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting continuous data simulator");


    }

    //
    //@Scheduled(fixedRateString = "${simulation.interval-ms}")
    public void sendMockData(){
        for (int i = 0; i < requestPerInterval; i++){
            EnergyUsageDto dto = EnergyUsageDto.builder()
                    .deviceId(random.nextLong(1,6))
                    .energyConsumed(Math.round(random.nextDouble(0.0,2.00) * 100.0) / 100.0)
                    .timestamp(LocalDateTime.now()
                            .atZone(ZoneId
                                    .systemDefault()).toInstant())
                    .build();

            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

                HttpEntity<EnergyUsageDto> request = new HttpEntity<>(dto, headers);
                restTemplate.postForEntity(ingestionEnpoint,request, Void.class);

                log.info("Sent mock data: {}", dto);
            } catch (Exception e) {
                log.error("Error occurred while sending mock data: {}", dto, e);
            }
        }

    }
}
