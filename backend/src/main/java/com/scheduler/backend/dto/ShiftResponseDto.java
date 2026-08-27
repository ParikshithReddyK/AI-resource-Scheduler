package com.scheduler.backend.dto;

import com.scheduler.backend.entity.ShiftStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftResponseDto {

    private UUID id;
    private String title;
    private UUID requiredSkillId;
    private String requiredSkillName;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String location;
    private ShiftStatus status;
}