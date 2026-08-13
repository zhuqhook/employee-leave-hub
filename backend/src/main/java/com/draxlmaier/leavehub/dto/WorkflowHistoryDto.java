package com.draxlmaier.leavehub.dto;

import com.draxlmaier.leavehub.entity.LeaveStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class WorkflowHistoryDto {
    private LeaveStatus oldStatus;
    private LeaveStatus currentStatus;
    private String changedByName;
    private LocalDateTime changedAt;
    private String comment;
}
