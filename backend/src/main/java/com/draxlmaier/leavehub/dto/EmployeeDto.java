package com.draxlmaier.leavehub.dto;

import com.draxlmaier.leavehub.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDto {
    private Long emplId;
    private String name;
    private String email;
    private Role role;
    private Long deptId;
    private String departmentName;
    private Integer annualLeaveDays;
    private Integer availableLeaveDays;
    private boolean active;
}
