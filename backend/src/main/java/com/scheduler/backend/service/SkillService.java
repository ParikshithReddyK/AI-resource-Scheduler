package com.scheduler.backend.service;

import com.scheduler.backend.dto.SkillRequestDto;
import com.scheduler.backend.dto.SkillResponseDto;
import com.scheduler.backend.entity.Skill;
import com.scheduler.backend.exception.ResourceNotFoundException;
import com.scheduler.backend.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SkillService {

    private final SkillRepository skillRepository;

    @Transactional
    public SkillResponseDto createSkill(SkillRequestDto request) {
        Skill skill = Skill.builder()
                .name(request.getName())
                .category(request.getCategory())
                .build();

        Skill saved = skillRepository.save(skill);
        return toResponseDto(saved);
    }

    @Transactional(readOnly = true)
    public SkillResponseDto getSkillById(UUID id) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found: " + id));
        return toResponseDto(skill);
    }

    @Transactional(readOnly = true)
    public List<SkillResponseDto> getAllSkills() {
        return skillRepository.findAll().stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    private SkillResponseDto toResponseDto(Skill skill) {
        return SkillResponseDto.builder()
                .id(skill.getId())
                .name(skill.getName())
                .category(skill.getCategory())
                .build();
    }
}