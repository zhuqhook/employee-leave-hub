package com.draxlmaier.leavehub.repository;

import com.draxlmaier.leavehub.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    boolean existsByDepartmentNameIgnoreCase(String departmentName);
}
