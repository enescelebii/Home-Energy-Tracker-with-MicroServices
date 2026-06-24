package com.vena.alertservice.service;

import com.vena.alertservice.entity.Alert;
import com.vena.alertservice.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final AlertRepository alertRepository;
    private final JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String body, Long userId) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setFrom("noreply@vena.com");
        message.setSubject(subject);
        message.setText(body);

        try {
            mailSender.send(message);

            // save it on SQL DB
            alertRepository.saveAndFlush(
                    Alert.builder()
                            .sent(true)
                            .message(body)
                            .userId(userId)
                            .createdAt(LocalDateTime.now())
                            .build()
            );

        } catch (Exception e) {
            log.error("Error occurred while sending email to {}: {}", to, e.getMessage());

            final Alert alertSend = Alert.builder()
                    .sent(false)
                    .message(e.getMessage())
                    .userId(userId)
                    .createdAt(LocalDateTime.now())
                    .build();
            alertRepository.saveAndFlush(alertSend);
        }

        log.info("Email sent to {} with subject: {}", to, subject);

    }

}
