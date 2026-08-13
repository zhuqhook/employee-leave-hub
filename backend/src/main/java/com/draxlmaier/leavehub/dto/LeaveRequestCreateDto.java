package com.draxlmaier.leavehub.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Getter
@Setter
public class LeaveRequestCreateDto {

    @NotNull
    private Long leaveTypeId;

    @NotNull
    @FutureOrPresent
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    /** true = trimite direct spre aprobare (PENDING); false = salveaza ca DRAFT. */
    private boolean submit = true;
}
