package com.draxlmaier.leavehub.controller;

import com.draxlmaier.leavehub.dto.DepartmentDto;
import com.draxlmaier.leavehub.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    /** Toti utilizatorii autentificati au nevoie de lista departamentelor (ex: filtre, formulare). */
    @GetMapping
    public List<DepartmentDto> findAll() {
        return departmentService.findAll();
    }

    @GetMapping("/{id}")
    public DepartmentDto findById(@PathVariable Long id) {
        return departmentService.findById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public DepartmentDto create(@Valid @RequestBody DepartmentDto dto) {
        return departmentService.create(dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public DepartmentDto update(@PathVariable Long id, @Valid @RequestBody DepartmentDto dto) {
        return departmentService.update(id, dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        departmentService.delete(id);
    }
}
