package com.draxlmaier.leavehub.service;

import com.draxlmaier.leavehub.dto.DepartmentDto;
import com.draxlmaier.leavehub.entity.Department;
import com.draxlmaier.leavehub.entity.Employee;
import com.draxlmaier.leavehub.exception.BusinessException;
import com.draxlmaier.leavehub.exception.ResourceNotFoundException;
import com.draxlmaier.leavehub.repository.DepartmentRepository;
import com.draxlmaier.leavehub.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    public List<DepartmentDto> findAll() {
        return departmentRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public DepartmentDto findById(Long id) {
        return toDto(getOrThrow(id));
    }

    public Department getOrThrow(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Departamentul cu id-ul " + id + " nu a fost gasit."));
    }

    @Transactional
    public DepartmentDto create(DepartmentDto dto) {
        if (departmentRepository.existsByDepartmentNameIgnoreCase(dto.getDepartmentName())) {
            throw new BusinessException("Exista deja un departament cu numele " + dto.getDepartmentName());
        }
        Department department = Department.builder()
                .departmentName(dto.getDepartmentName())
                .manager(resolveManager(dto.getManagerId()))
                .maxAbsentEmployees(dto.getMaxAbsentEmployees())
                .build();
        return toDto(departmentRepository.save(department));
    }

    @Transactional
    public DepartmentDto update(Long id, DepartmentDto dto) {
        Department department = getOrThrow(id);
        department.setDepartmentName(dto.getDepartmentName());
        department.setManager(resolveManager(dto.getManagerId()));
        department.setMaxAbsentEmployees(dto.getMaxAbsentEmployees());
        return toDto(departmentRepository.save(department));
    }

    @Transactional
    public void delete(Long id) {
        Department department = getOrThrow(id);
        if (!employeeRepository.findByDepartment_DeptId(id).isEmpty()) {
            throw new BusinessException("Departamentul nu poate fi sters cat timp are angajati asociati.");
        }
        departmentRepository.delete(department);
    }

    private Employee resolveManager(Long managerId) {
        if (managerId == null) {
            return null;
        }
        return employeeRepository.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException("Angajatul cu id-ul " + managerId + " nu a fost gasit."));
    }

    public DepartmentDto toDto(Department d) {
        return DepartmentDto.builder()
                .deptId(d.getDeptId())
                .departmentName(d.getDepartmentName())
                .managerId(d.getManager() != null ? d.getManager().getEmplId() : null)
                .managerName(d.getManager() != null ? d.getManager().getName() : null)
                .maxAbsentEmployees(d.getMaxAbsentEmployees())
                .build();
    }
}
