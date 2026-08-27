package com.scheduler.backend.service;

import com.scheduler.backend.dto.ShiftRequestDto;
import com.scheduler.backend.dto.ShiftResponseDto;
import com.scheduler.backend.entity.Shift;
import com.scheduler.backend.entity.ShiftStatus;
import com.scheduler.backend.entity.Skill;
import com.scheduler.backend.exception.ResourceNotFoundException;
import com.scheduler.backend.repository.ShiftRepository;
import com.scheduler.backend.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShiftService {

    private final ShiftRepository shiftRepository;
    private final SkillRepository skillRepository;

    @Transactional
    public ShiftResponseDto createShift(ShiftRequestDto request) {
        Skill requiredSkill = skillRepository.findById(request.getRequiredSkillId())
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found: " + request.getRequiredSkillId()));

        Shift shift = Shift.builder()
                .title(request.getTitle())
                .requiredSkill(requiredSkill)
                .date(request.getDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .location(request.getLocation())
                .status(ShiftStatus.OPEN)
                .build();

        Shift saved = shiftRepository.save(shift);
        return toResponseDto(saved);
    }

    @Transactional(readOnly = true)
    public ShiftResponseDto getShiftById(UUID id) {
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found: " + id));
        return toResponseDto(shift);
    }

    @Transactional(readOnly = true)
    public List<ShiftResponseDto> getAllShifts() {
        return shiftRepository.findAll().stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ShiftResponseDto> getShiftsByStatus(ShiftStatus status) {
        return shiftRepository.findByStatus(status).stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    private ShiftResponseDto toResponseDto(Shift shift) {
        return ShiftResponseDto.builder()
                .id(shift.getId())
                .title(shift.getTitle())
                .requiredSkillId(shift.getRequiredSkill().getId())
                .requiredSkillName(shift.getRequiredSkill().getName())
                .date(shift.getDate())
                .startTime(shift.getStartTime())
                .endTime(shift.getEndTime())
                .location(shift.getLocation())
                .status(shift.getStatus())
                .build();
    }
}