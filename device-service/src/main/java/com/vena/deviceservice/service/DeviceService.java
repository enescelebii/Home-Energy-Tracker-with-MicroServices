package com.vena.deviceservice.service;


import com.vena.deviceservice.dto.DeviceDto;
import com.vena.deviceservice.entity.Device;
import com.vena.deviceservice.exception.DeviceNotFoundException;
import com.vena.deviceservice.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceService {
    private final DeviceRepository deviceRepository;

    public DeviceDto getDeviceById(Long id) {
        Device device = deviceRepository.findById(id).orElseThrow(() -> new DeviceNotFoundException("Device not found with id: " + id));
        return toDto(device);
    }

    public List<DeviceDto> getDevicesByUserId(Long userId) {
        List<Device> devices = deviceRepository.findByUserId(userId);
        return devices.stream().map(this::toDto).toList();
    }

    private DeviceDto toDto(Device device) {
        return DeviceDto.builder()
                .id(device.getId())
                .name(device.getName())
                .type(device.getType())
                .location(device.getLocation())
                .userId(device.getUserId())
                .build();
    }

    public DeviceDto createDevice(DeviceDto deviceDto) {
        Device device = Device.builder()
                .name(deviceDto.getName())
                .type(deviceDto.getType())
                .location(deviceDto.getLocation())
                .userId(deviceDto.getUserId())
                .build();
        Device savedDevice = deviceRepository.save(device);
        return toDto(savedDevice);
    }

    public DeviceDto updateDevice(Long id, DeviceDto deviceDto) {
        Device device = deviceRepository.findById(id).orElseThrow(() -> new DeviceNotFoundException("Device not found with id: " + id));
        device.setName(deviceDto.getName());
        device.setType(deviceDto.getType());
        device.setLocation(deviceDto.getLocation());
        Device updatedDevice = deviceRepository.save(device);
        return toDto(updatedDevice);
    }

    public void deleteDevice(long id) {
        Device device = deviceRepository.findById(id).orElseThrow(() -> new DeviceNotFoundException("Device not found with id: " + id));
        deviceRepository.delete(device);
        ResponseEntity.noContent().build();
    }
}
