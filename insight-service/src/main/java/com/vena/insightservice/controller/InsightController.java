package com.vena.insightservice.controller;


import com.vena.insightservice.dto.InsightDto;
import com.vena.insightservice.service.InsightService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/insights")
public class InsightController {

    public final InsightService insightService;

    @GetMapping("/saving-tips/{userId}")
    public ResponseEntity<InsightDto> getSavingsTips(@PathVariable Long userId) {
        log.info("Fetching savings tips for user: {}", userId);
        InsightDto insight = insightService.getSavingsTips(userId);
        return ResponseEntity.ok(insight);
    }

    @GetMapping("/overview/{userId}")
    public ResponseEntity<InsightDto> getOverviwev(@PathVariable Long userId){
        final InsightDto insight = insightService.getOverview(userId);
        return ResponseEntity.ok(insight);
    }




}
