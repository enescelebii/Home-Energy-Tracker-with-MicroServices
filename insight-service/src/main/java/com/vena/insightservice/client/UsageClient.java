package com.vena.insightservice.client;


import com.vena.insightservice.dto.UsageDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@Slf4j
public class UsageClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${usage.service.url}")
    private String usageServiceUrl;


    public UsageDto getXDaysUsageForUser (Long userId, int days) {
        String url = UriComponentsBuilder
                .fromUriString(usageServiceUrl)
                .path("/{userId}")
                .queryParam("days", days)
                .buildAndExpand(userId)
                .toUriString();

        ResponseEntity<UsageDto> response = restTemplate.getForEntity(url, UsageDto.class);
        if (response.getBody() == null) {
            log.warn("No usage data found for userId: {} and days: {}", userId, days);
            return response.getBody(); // Return an empty UsageDto or handle as needed
        }
        return response.getBody();
    }

}
