package com.draxlmaier.leavehub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class DepartmentStatsDto {
    private Long deptId;
    private String departmentName;
    private long totalRequests;
    private long pendingRequests;
    private long approvedRequests;
    private long rejectedRequests;
    private long totalDaysConsumed;
    private int employeeCount;
}
