package com.vena.usageservice.client;


import com.vena.usageservice.dto.DeviceDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import org.springframework.web.util.UriComponentsBuilder;

@Component
public class DeviceClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String deviceServiceUrl;

    public DeviceClient(@Value("${device.service.url}") String deviceServiceUrl) {
        this.deviceServiceUrl = deviceServiceUrl;

    }

    public DeviceDto getDeviceById(Long id){
        String url = UriComponentsBuilder
                .fromUriString(deviceServiceUrl)
                .path("/{id}")
                .buildAndExpand(id)
                .toUriString();
        ResponseEntity<DeviceDto> response = restTemplate.getForEntity(url, DeviceDto.class);
        return response.getBody();
    }

}
