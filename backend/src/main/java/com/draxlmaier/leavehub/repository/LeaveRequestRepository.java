package com.draxlmaier.leavehub.repository;

import com.draxlmaier.leavehub.entity.LeaveRequest;
import com.draxlmaier.leavehub.entity.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByEmployee_EmplIdOrderByCreatedAtDesc(Long emplId);

    List<LeaveRequest> findByEmployee_Department_DeptIdOrderByStartDateDesc(Long deptId);

    @Query("select r from LeaveRequest r where " +
            "(:deptId is null or r.employee.department.deptId = :deptId) and " +
            "(:status is null or r.status = :status) and " +
            "(:leaveTypeId is null or r.leaveType.leaveTypeId = :leaveTypeId) and " +
            "(:emplId is null or r.employee.emplId = :emplId) and " +
            "(:from is null or r.endDate >= :from) and " +
            "(:to is null or r.startDate <= :to) " +
            "order by r.createdAt desc")
    List<LeaveRequest> search(@Param("deptId") Long deptId,
                               @Param("status") LeaveStatus status,
                               @Param("leaveTypeId") Long leaveTypeId,
                               @Param("emplId") Long emplId,
                               @Param("from") LocalDate from,
                               @Param("to") LocalDate to);

    /**
     * Cererile APROBATE dintr-un departament care se suprapun cu perioada data.
     * Folosita pentru verificarea numarului maxim de angajati absenti simultan
     * si pentru vizualizarea calendarului comun.
     */
    @Query("select r from LeaveRequest r where r.employee.department.deptId = :deptId " +
            "and r.status = com.draxlmaier.leavehub.entity.LeaveStatus.APPROVED " +
            "and r.startDate <= :to and r.endDate >= :from")
    List<LeaveRequest> findApprovedOverlapping(@Param("deptId") Long deptId,
                                                @Param("from") LocalDate from,
                                                @Param("to") LocalDate to);
}
