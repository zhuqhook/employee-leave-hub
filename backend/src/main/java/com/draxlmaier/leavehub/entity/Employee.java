package com.draxlmaier.leavehub.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "employee")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "empl_id")
    private Long emplId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    /** Hash-ul parolei (BCrypt) - nu este niciodata expus in raspunsurile API. */
    @JsonIgnore
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // EAGER in mod deliberat: angajatul autentificat (incarcat la login) este pastrat in
    // contextul de securitate si folosit ulterior in alte cereri; incarcarea "leneasa" (LAZY)
    // a departamentului esueaza in acel moment cu "no Session", pentru ca sesiunea originala
    // in care a fost incarcat angajatul s-a inchis deja. Department e o entitate mica, deci
    // incarcarea ei imediata (EAGER) nu are impact notabil asupra performantei.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "dept_id")
    private Department department;

    @Column(name = "annual_leave_days", nullable = false)
    private Integer annualLeaveDays;

    @Column(name = "available_leave_days", nullable = false)
    private Integer availableLeaveDays;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
