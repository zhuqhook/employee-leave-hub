package com.draxlmaier.leavehub.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Istoricul tranzitiilor de status pentru o cerere de concediu.
 * O intrare noua este creata la fiecare schimbare de status (submit, approve, reject, cancel).
 */
@Entity
@Table(name = "leave_workflow")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveWorkflow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "workflow_id")
    private Long workflowId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "leave_request_id", nullable = false)
    private LeaveRequest leaveRequest;

    /** Angajatul care a facut schimbarea (poate fi angajatul insusi sau responsabilul/adminul). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empl_id", nullable = false)
    private Employee changedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", length = 20)
    private LeaveStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_status", nullable = false, length = 20)
    private LeaveStatus currentStatus;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    /** Comentariu, obligatoriu la respingere. */
    @Column(length = 1000)
    private String comment;
}
