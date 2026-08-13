package com.draxlmaier.leavehub.dto;

import com.draxlmaier.leavehub.entity.LeaveStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class LeaveRequestDto {
    private Long leaveRequestId;
    private Long emplId;
    private String employeeName;
    private Long deptId;
    private String departmentName;
    private Long leaveTypeId;
    private String leaveTypeName;
    private String leaveTypeCode;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer workingDays;
    private LeaveStatus status;
    private LocalDateTime createdAt;
    private List<AttachmentDto> attachments;
    private List<WorkflowHistoryDto> history;
}
