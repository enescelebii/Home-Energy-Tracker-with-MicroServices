package com.vena.ingestionservice.simulation;


import com.vena.ingestionservice.dto.EnergyUsageDto;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Component
@Slf4j
@RequiredArgsConstructor
public class ParallelDataSimulation implements CommandLineRunner {

    private final Random random = new Random();
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${simulation.parallel-threads}")
    private int parallelThreads;
    @Value("${simulation.request-per-interval}")
    private int requestsPerInterval;
    @Value("${simulation.ingestion-endpoint}")
    private String ingestionEndpoint;

    private final ExecutorService executorService;

    public ParallelDataSimulation(){
        this.executorService = Executors.newCachedThreadPool();
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting parallel data simulation");
        ((ThreadPoolExecutor)executorService).setCorePoolSize(parallelThreads);
    }

    @Scheduled(fixedRateString = "${simulation.interval-ms}")
    public void sendParallelData(){
        log.info("Sending parallel data");
        int batchSize = requestsPerInterval / parallelThreads;
        int remainder = requestsPerInterval % parallelThreads;

        for (int i = 0; i < parallelThreads; i++){
            int requestsForThread = batchSize + (i < remainder ? 1 : 0);
            executorService.submit(() -> {
                // Simulate sending data for this thread
                for (int j = 0; j < requestsForThread; j++){
                    EnergyUsageDto dto = EnergyUsageDto.builder()
                            .deviceId(random.nextLong(1,200))
                            .energyConsumed(Math.round(random.nextDouble(0.0,2.00) * 100.0) / 100.0)
                            .timestamp(LocalDateTime.now()
                                    .atZone(ZoneId
                                            .systemDefault()).toInstant())
                            .build();
                    try {
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
                        HttpEntity<EnergyUsageDto> request = new HttpEntity<>(dto, headers);
                        restTemplate.postForEntity(ingestionEndpoint, request, Void.class);
                        System.out.println("Thread " + Thread.currentThread().getName() + " sent: " + dto);

                    } catch (Exception e) {
                        log.error("Error occurred while sending mock data: {}", dto, e);
                    }
                }
            });
        }
    }

    @PreDestroy
    public void shutdown(){
        executorService.shutdown();
        log.info("Parallel data simulation stopped");
    }
}
