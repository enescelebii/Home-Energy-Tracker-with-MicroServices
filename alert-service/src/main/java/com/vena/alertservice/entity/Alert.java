package com.vena.alertservice.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "alert")
public class Alert {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(nullable = false)
    private Long id;

    private String message;
    private Boolean sent;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    private Long userId;

}
