package com.draxlmaier.leavehub.service;

import com.draxlmaier.leavehub.dto.EmployeeCreateDto;
import com.draxlmaier.leavehub.dto.EmployeeDto;
import com.draxlmaier.leavehub.entity.Department;
import com.draxlmaier.leavehub.entity.Employee;
import com.draxlmaier.leavehub.exception.BusinessException;
import com.draxlmaier.leavehub.exception.ResourceNotFoundException;
import com.draxlmaier.leavehub.repository.DepartmentRepository;
import com.draxlmaier.leavehub.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    public List<EmployeeDto> findAll() {
        return employeeRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public EmployeeDto findById(Long id) {
        return toDto(getOrThrow(id));
    }

    public Employee getOrThrow(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Angajatul cu id-ul " + id + " nu a fost gasit."));
    }

    @Transactional
    public EmployeeDto create(EmployeeCreateDto dto) {
        if (employeeRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessException("Exista deja un angajat cu email-ul " + dto.getEmail());
        }
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new BusinessException("Parola este obligatorie la crearea unui angajat.");
        }

        Department department = resolveDepartment(dto.getDeptId());

        Employee employee = Employee.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .role(dto.getRole())
                .department(department)
                .annualLeaveDays(dto.getAnnualLeaveDays())
                .availableLeaveDays(dto.getAnnualLeaveDays())
                .active(dto.getActive() == null || dto.getActive())
                .build();

        return toDto(employeeRepository.save(employee));
    }

    @Transactional
    public EmployeeDto update(Long id, EmployeeCreateDto dto) {
        Employee employee = getOrThrow(id);

        if (!employee.getEmail().equalsIgnoreCase(dto.getEmail())
                && employeeRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessException("Exista deja un angajat cu email-ul " + dto.getEmail());
        }

        employee.setName(dto.getName());
        employee.setEmail(dto.getEmail());
        employee.setRole(dto.getRole());
        employee.setDepartment(resolveDepartment(dto.getDeptId()));
        employee.setAnnualLeaveDays(dto.getAnnualLeaveDays());
        if (dto.getActive() != null) {
            employee.setActive(dto.getActive());
        }
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            employee.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }

        return toDto(employeeRepository.save(employee));
    }

    @Transactional
    public void delete(Long id) {
        Employee employee = getOrThrow(id);
        // Soft delete: dezactivam contul in loc sa stergem, pentru a pastra istoricul cererilor.
        employee.setActive(false);
        employeeRepository.save(employee);
    }

    private Department resolveDepartment(Long deptId) {
        if (deptId == null) {
            return null;
        }
        return departmentRepository.findById(deptId)
                .orElseThrow(() -> new ResourceNotFoundException("Departamentul cu id-ul " + deptId + " nu a fost gasit."));
    }

    public EmployeeDto toDto(Employee e) {
        return EmployeeDto.builder()
                .emplId(e.getEmplId())
                .name(e.getName())
                .email(e.getEmail())
                .role(e.getRole())
                .deptId(e.getDepartment() != null ? e.getDepartment().getDeptId() : null)
                .departmentName(e.getDepartment() != null ? e.getDepartment().getDepartmentName() : null)
                .annualLeaveDays(e.getAnnualLeaveDays())
                .availableLeaveDays(e.getAvailableLeaveDays())
                .active(e.isActive())
                .build();
    }
}
