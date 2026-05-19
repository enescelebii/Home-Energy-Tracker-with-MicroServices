package com.vena.deviceservice;

import com.vena.deviceservice.model.DeviceType;
import com.vena.deviceservice.repository.DeviceRepository;
import com.vena.deviceservice.service.DeviceService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
class DeviceServiceApplicationTests {

    public static final int NUMBER_OF_DEVICES = 200;
    public static final int USERS = 10;

    @Autowired
    private DeviceRepository deviceRepository;

    @Test
    void contextLoads() {
    }

    @Test
    void createDevice() {
        for (int i = 1; i <= NUMBER_OF_DEVICES; i++) {
            deviceRepository.save(com.vena.deviceservice.entity.Device.builder()
                    .name("Device " + i)
                    .type(DeviceType.values()[i % DeviceType.values().length])
                    .location("Location " + ((i % 3) + 1))
                    .userId((long) ((i % USERS) + 1))
                    .build());
        }
        log.info("Devices Repository has been populated");
    }

}
