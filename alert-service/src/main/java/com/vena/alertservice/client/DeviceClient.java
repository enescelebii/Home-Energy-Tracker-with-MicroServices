package com.vena.alertservice.client;


import com.vena.alertservice.dto.DeviceDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;


@Component
public class DeviceClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String deviceServiceUrl;

    public DeviceClient(@Value("${device.service.url}") String deviceServiceUrl) {
        this.deviceServiceUrl = deviceServiceUrl;

    }

    public List<DeviceDto> getDevicesById(Long userId){
        String url = UriComponentsBuilder
                .fromUriString(deviceServiceUrl)
                .path("/user/{userId}")
                .buildAndExpand(userId)
                .toUriString();
        ResponseEntity<DeviceDto[]> response = restTemplate.getForEntity(url, DeviceDto[].class);
        if(response.getBody() == null){
            return Collections.emptyList();
        }
        return List.of(response.getBody());
    }

}
