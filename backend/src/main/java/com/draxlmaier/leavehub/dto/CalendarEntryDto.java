package com.draxlmaier.leavehub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class CalendarEntryDto {
    private Long leaveRequestId;
    private Long emplId;
    private String employeeName;
    private String leaveTypeCode;
    private LocalDate startDate;
    private LocalDate endDate;
}
