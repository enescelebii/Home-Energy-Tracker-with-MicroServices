
package com.vena.alertservice.client;

import com.vena.alertservice.dto.UserDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class UserClient {

    private final RestTemplate restTemplate;
    private final String userServiceUrl;

    public UserClient(@Value("${user.service.url}") String userServiceUrl) {
        this.restTemplate = new RestTemplate();
        this.userServiceUrl = userServiceUrl;
    }

    public UserDto getUserById(Long id){
        String url = UriComponentsBuilder
                .fromUriString(userServiceUrl)
                .path("/{id}")
                .buildAndExpand(id)
                .toUriString();

        ResponseEntity<UserDto> response = restTemplate.getForEntity(url, UserDto.class);
        return response.getBody();
    }

}
