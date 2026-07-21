package com.vena.user_service;

import com.vena.user_service.entity.User;
import com.vena.user_service.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Service;

@SpringBootTest
@Slf4j
@Disabled
class UserServiceApplicationTests {

	public static final int NUMBER_OF_USERS = 10;
	@Autowired
	private UserRepository userRepository;

	@Test
	void contextLoads() {
	}

	@Disabled
	@Test
	void createUser() {
		for (int i = 1; i <= NUMBER_OF_USERS; i++) {
			userRepository.save(User.builder()
					.name("User " + i)
					.surname("Surname " + i)
					.email("user" + i + "@example.com")
					.address("Address " + i)
					.alerting(i % 2 == 0)
					.energyAlertingThreshold(100.0 + i * 10)
					.build());
		}
		log.info("User Repository has been populated");
	}

}
