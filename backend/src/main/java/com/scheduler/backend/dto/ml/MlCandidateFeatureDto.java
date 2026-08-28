package com.scheduler.backend.dto.ml;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MlCandidateFeatureDto {

    @JsonProperty("employee_id")
    private String employeeId;

    @JsonProperty("workload_count")
    private int workloadCount;

    @JsonProperty("days_since_last_assignment")
    private int daysSinceLastAssignment;
}