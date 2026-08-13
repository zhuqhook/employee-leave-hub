package com.draxlmaier.leavehub.entity;

/**
 * Ciclul de viata al unei cereri de concediu.
 * DRAFT -> PENDING -> APPROVED | REJECTED
 * DRAFT/PENDING -> CANCELLED (doar de catre angajat, cat timp cererea nu a fost aprobata)
 */
public enum LeaveStatus {
    DRAFT,
    PENDING,
    APPROVED,
    REJECTED,
    CANCELLED
}
