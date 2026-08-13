package com.draxlmaier.leavehub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentDto {
    private Long deptId;

    @NotBlank
    private String departmentName;

    private Long managerId;
    private String managerName;

    @NotNull
    private Integer maxAbsentEmployees;
}
