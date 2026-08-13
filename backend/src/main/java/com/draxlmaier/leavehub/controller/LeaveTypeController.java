package com.draxlmaier.leavehub.controller;

import com.draxlmaier.leavehub.dto.LeaveTypeDto;
import com.draxlmaier.leavehub.service.LeaveTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/leave-types")
@RequiredArgsConstructor
public class LeaveTypeController {

    private final LeaveTypeService leaveTypeService;

    @GetMapping
    public List<LeaveTypeDto> findAll() {
        return leaveTypeService.findAll();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public LeaveTypeDto create(@Valid @RequestBody LeaveTypeDto dto) {
        return leaveTypeService.create(dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public LeaveTypeDto update(@PathVariable Long id, @Valid @RequestBody LeaveTypeDto dto) {
        return leaveTypeService.update(id, dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        leaveTypeService.delete(id);
    }
}
