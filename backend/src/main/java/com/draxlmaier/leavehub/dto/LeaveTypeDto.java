package com.draxlmaier.leavehub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveTypeDto {
    private Long leaveTypeId;

    @NotBlank
    private String name;

    @NotBlank
    private String code;

    private boolean requiresAttachment;
    private boolean paid;
}
