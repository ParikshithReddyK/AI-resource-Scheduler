package com.scheduler.backend.controller;

import com.scheduler.backend.dto.CandidateResponseDto;
import com.scheduler.backend.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/shifts/{shiftId}/candidates")
    public ResponseEntity<List<CandidateResponseDto>> getCandidates(@PathVariable UUID shiftId) {
        return ResponseEntity.ok(recommendationService.getCandidatesForShift(shiftId));
    }
}