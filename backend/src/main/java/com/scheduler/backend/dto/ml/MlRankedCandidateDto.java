package com.scheduler.backend.dto.ml;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MlRankedCandidateDto {

    @JsonProperty("employee_id")
    private String employeeId;

    private double score;
    private Map<String, Double> explanation;
}