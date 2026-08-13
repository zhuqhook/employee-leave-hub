package com.draxlmaier.leavehub.controller;

import com.draxlmaier.leavehub.dto.EmployeeCreateDto;
import com.draxlmaier.leavehub.dto.EmployeeDto;
import com.draxlmaier.leavehub.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    /** Vizibil pentru manageri/admin - necesar la afisarea listelor de cereri, alocarea managerilor etc. */
    @PreAuthorize("hasAnyRole('ADMIN','DEPARTMENT_MANAGER')")
    @GetMapping
    public List<EmployeeDto> findAll() {
        return employeeService.findAll();
    }

    @PreAuthorize("hasAnyRole('ADMIN','DEPARTMENT_MANAGER')")
    @GetMapping("/{id}")
    public EmployeeDto findById(@PathVariable Long id) {
        return employeeService.findById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public EmployeeDto create(@Valid @RequestBody EmployeeCreateDto dto) {
        return employeeService.create(dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public EmployeeDto update(@PathVariable Long id, @Valid @RequestBody EmployeeCreateDto dto) {
        return employeeService.update(id, dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        employeeService.delete(id);
    }
}
