package com.draxlmaier.leavehub.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LeaveDecisionDto {
    /** Obligatoriu doar la respingere (validat in service). */
    private String comment;
}
