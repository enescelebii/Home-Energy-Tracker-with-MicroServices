package com.vena.user_service.integration;

import com.vena.user_service.dto.UserDto;
import com.vena.user_service.entity.User;
import com.vena.user_service.repository.UserRepository;
import com.vena.user_service.testsupport.MySqlTestContainerBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@AutoConfigureTestRestTemplate
@ActiveProfiles("test")
class UserServiceIntegrationTest extends MySqlTestContainerBase {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    // =========================================================
    // CREATE + GET
    // =========================================================

    @Test
    void createUser_viaRestApi_persistsAndReturnsUser() {

        // Arrange
        UserDto request = UserDto.builder()
                .name("Leet")
                .surname("Journey")
                .email("leetjourney@gmail.com")
                .address("123 Coding St")
                .alerting(true)
                .energyAlertingThreshold(2000.0)
                .build();

        // Act
        ResponseEntity<UserDto> response =
                restTemplate.postForEntity(
                        "/api/v1/user",
                        request,
                        UserDto.class
                );

        // Assert - HTTP response
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.CREATED);

        assertThat(response.getBody())
                .isNotNull();

        UserDto createdUser = response.getBody();

        assertThat(createdUser.getId())
                .isNotNull();

        assertThat(createdUser.getName())
                .isEqualTo("Leet");

        assertThat(createdUser.getSurname())
                .isEqualTo("Journey");

        assertThat(createdUser.getEmail())
                .isEqualTo("leetjourney@gmail.com");

        assertThat(createdUser.getAddress())
                .isEqualTo("123 Coding St");

        assertThat(createdUser.isAlerting())
                .isTrue();

        assertThat(createdUser.getEnergyAlertingThreshold())
                .isEqualTo(2000.0);

        // Database kontrolü
        User userFromDatabase =
                userRepository.findById(createdUser.getId())
                        .orElse(null);

        assertThat(userFromDatabase)
                .isNotNull();

        assertThat(userFromDatabase.getEmail())
                .isEqualTo("leetjourney@gmail.com");

        assertThat(userFromDatabase.getAlerting())
                .isTrue();

        // GET endpoint kontrolü
        ResponseEntity<UserDto> loadedResponse =
                restTemplate.getForEntity(
                        "/api/v1/user/" + createdUser.getId(),
                        UserDto.class
                );

        assertThat(loadedResponse.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        assertThat(loadedResponse.getBody())
                .isNotNull();

        assertThat(loadedResponse.getBody().getId())
                .isEqualTo(createdUser.getId());

        assertThat(loadedResponse.getBody().getEmail())
                .isEqualTo("leetjourney@gmail.com");
    }

    // =========================================================
    // UPDATE
    // =========================================================

    @Test
    void updateUser_viaRestApi_updatesAndReturnsUser() {

        // Arrange
        User savedUser = userRepository.save(
                User.builder()
                        .name("Leet")
                        .surname("Journey")
                        .email("leetjourney@gmail.com")
                        .address("123 Coding St")
                        .alerting(true)
                        .energyAlertingThreshold(2000.0)
                        .build()
        );

        /*
         * Controller:
         *
         * @PutMapping
         * updateUser(@RequestBody UserDto userDto)
         *
         * kullandığı için ID request body içerisinde gönderiliyor.
         */
        UserDto updateRequest = UserDto.builder()
                .id(savedUser.getId())
                .name("Enes")
                .surname("Celebi")
                .email("enes@example.com")
                .address("Istanbul")
                .alerting(false)
                .energyAlertingThreshold(3500.0)
                .build();

        HttpEntity<UserDto> requestEntity =
                new HttpEntity<>(updateRequest);

        // Act
        ResponseEntity<UserDto> response =
                restTemplate.exchange(
                        "/api/v1/user",
                        HttpMethod.PUT,
                        requestEntity,
                        UserDto.class
                );

        // Assert - HTTP
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        assertThat(response.getBody())
                .isNotNull();

        UserDto updatedResponse = response.getBody();

        // Assert - Response
        assertThat(updatedResponse.getId())
                .isEqualTo(savedUser.getId());

        assertThat(updatedResponse.getName())
                .isEqualTo("Enes");

        assertThat(updatedResponse.getSurname())
                .isEqualTo("Celebi");

        assertThat(updatedResponse.getEmail())
                .isEqualTo("enes@example.com");

        assertThat(updatedResponse.getAddress())
                .isEqualTo("Istanbul");

        assertThat(updatedResponse.isAlerting())
                .isFalse();

        assertThat(updatedResponse.getEnergyAlertingThreshold())
                .isEqualTo(3500.0);

        // Assert - Database
        User updatedUser =
                userRepository.findById(savedUser.getId())
                        .orElse(null);

        assertThat(updatedUser)
                .isNotNull();

        assertThat(updatedUser.getName())
                .isEqualTo("Enes");

        assertThat(updatedUser.getSurname())
                .isEqualTo("Celebi");

        assertThat(updatedUser.getEmail())
                .isEqualTo("enes@example.com");

        assertThat(updatedUser.getAddress())
                .isEqualTo("Istanbul");

        assertThat(updatedUser.getAlerting())
                .isFalse();

        assertThat(updatedUser.getEnergyAlertingThreshold())
                .isEqualTo(3500.0);
    }

    // =========================================================
    // DELETE
    // =========================================================

    @Test
    void deleteUser_viaRestApi_removesUserFromDatabase() {

        // Arrange
        User savedUser = userRepository.save(
                User.builder()
                        .name("Leet")
                        .surname("Journey")
                        .email("leetjourney@gmail.com")
                        .address("123 Coding St")
                        .alerting(true)
                        .energyAlertingThreshold(2000.0)
                        .build()
        );

        Long userId = savedUser.getId();

        // Silmeden önce gerçekten var mı?
        assertThat(userRepository.existsById(userId))
                .isTrue();

        // Act
        ResponseEntity<Void> response =
                restTemplate.exchange(
                        "/api/v1/user/" + userId,
                        HttpMethod.DELETE,
                        null,
                        Void.class
                );

        // Assert - HTTP
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);

        // Assert - Database
        assertThat(userRepository.existsById(userId))
                .isFalse();
    }

    // =========================================================
    // REPOSITORY + MYSQL
    // =========================================================

    @Test
    void saveUser_viaRepository_roundTripsThroughMysql() {

        // Arrange + Act
        User savedUser = userRepository.save(
                User.builder()
                        .name("Leet")
                        .surname("Journey")
                        .email("leetjourney@gmail.com")
                        .address("123 Coding St")
                        .alerting(true)
                        .energyAlertingThreshold(2000.0)
                        .build()
        );

        assertThat(savedUser.getId())
                .isNotNull();

        // Database'den tekrar oku
        User userFromDatabase =
                userRepository.findById(savedUser.getId())
                        .orElse(null);

        // Assert
        assertThat(userFromDatabase)
                .isNotNull();

        assertThat(userFromDatabase.getName())
                .isEqualTo("Leet");

        assertThat(userFromDatabase.getSurname())
                .isEqualTo("Journey");

        assertThat(userFromDatabase.getEmail())
                .isEqualTo("leetjourney@gmail.com");

        assertThat(userFromDatabase.getAddress())
                .isEqualTo("123 Coding St");

        assertThat(userFromDatabase.getAlerting())
                .isTrue();

        assertThat(userFromDatabase.getEnergyAlertingThreshold())
                .isEqualTo(2000.0);
    }
}