package com.draxlmaier.leavehub.entity;

/**
 * Roles supported by the application.
 * USER          -> regular employee, can create/view/cancel own leave requests
 * DEPARTMENT_MANAGER -> responsabil de departament, approves/rejects requests for their department
 * ADMIN         -> full access, manages employees/departments/leave types
 */
public enum Role {
    USER,
    DEPARTMENT_MANAGER,
    ADMIN
}
