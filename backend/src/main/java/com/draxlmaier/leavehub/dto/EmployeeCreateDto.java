package com.draxlmaier.leavehub.dto;

import com.draxlmaier.leavehub.entity.Role;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/** Folosit de administrator pentru a crea sau actualiza un angajat. */
@Getter
@Setter
public class EmployeeCreateDto {

    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;

    /** Obligatoriu la creare; ignorat/optional la actualizare. */
    private String password;

    @NotNull
    private Role role;

    private Long deptId;

    @NotNull
    private Integer annualLeaveDays;

    private Boolean active;
}
