package com.scheduler.backend.dto.ml;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MlRecommendRequestDto {

    @JsonProperty("shift_id")
    private String shiftId;

    private List<MlCandidateFeatureDto> candidates;
}