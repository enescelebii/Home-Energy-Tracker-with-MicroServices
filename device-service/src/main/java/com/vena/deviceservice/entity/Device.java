package com.vena.deviceservice.entity;


import com.vena.deviceservice.model.DeviceType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "device")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private DeviceType type;
    private String location;
    private Long userId;
}
