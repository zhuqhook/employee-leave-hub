package com.draxlmaier.leavehub.service;

import com.draxlmaier.leavehub.dto.LeaveTypeDto;
import com.draxlmaier.leavehub.entity.LeaveType;
import com.draxlmaier.leavehub.exception.BusinessException;
import com.draxlmaier.leavehub.exception.ResourceNotFoundException;
import com.draxlmaier.leavehub.repository.LeaveTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveTypeService {

    private final LeaveTypeRepository leaveTypeRepository;

    public List<LeaveTypeDto> findAll() {
        return leaveTypeRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public LeaveType getOrThrow(Long id) {
        return leaveTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipul de concediu cu id-ul " + id + " nu a fost gasit."));
    }

    @Transactional
    public LeaveTypeDto create(LeaveTypeDto dto) {
        if (leaveTypeRepository.existsByCodeIgnoreCase(dto.getCode())) {
            throw new BusinessException("Exista deja un tip de concediu cu codul " + dto.getCode());
        }
        LeaveType leaveType = LeaveType.builder()
                .name(dto.getName())
                .code(dto.getCode().toUpperCase())
                .requiresAttachment(dto.isRequiresAttachment())
                .paid(dto.isPaid())
                .build();
        return toDto(leaveTypeRepository.save(leaveType));
    }

    @Transactional
    public LeaveTypeDto update(Long id, LeaveTypeDto dto) {
        LeaveType leaveType = getOrThrow(id);
        leaveType.setName(dto.getName());
        leaveType.setCode(dto.getCode().toUpperCase());
        leaveType.setRequiresAttachment(dto.isRequiresAttachment());
        leaveType.setPaid(dto.isPaid());
        return toDto(leaveTypeRepository.save(leaveType));
    }

    @Transactional
    public void delete(Long id) {
        leaveTypeRepository.delete(getOrThrow(id));
    }

    public LeaveTypeDto toDto(LeaveType lt) {
        return LeaveTypeDto.builder()
                .leaveTypeId(lt.getLeaveTypeId())
                .name(lt.getName())
                .code(lt.getCode())
                .requiresAttachment(lt.isRequiresAttachment())
                .paid(lt.isPaid())
                .build();
    }
}
