package com.scheduler.backend.service;

import com.scheduler.backend.dto.ml.MlRecommendRequestDto;
import com.scheduler.backend.dto.ml.MlRecommendResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class MlRecommendationClient {

    private final RestClient mlServiceRestClient;

    public MlRecommendResponseDto rank(MlRecommendRequestDto request) {
        return mlServiceRestClient.post()
                .uri("/recommend")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(MlRecommendResponseDto.class);
    }
}