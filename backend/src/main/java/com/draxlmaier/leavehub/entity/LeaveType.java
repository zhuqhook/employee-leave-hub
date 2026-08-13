package com.draxlmaier.leavehub.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "leave_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "leave_type_id")
    private Long leaveTypeId;

    @Column(nullable = false)
    private String name;

    /** CO / CM / FP / SPECIAL */
    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(name = "requires_attachment", nullable = false)
    @Builder.Default
    private boolean requiresAttachment = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean paid = true;
}
