package com.draxlmaier.leavehub.repository;

import com.draxlmaier.leavehub.entity.LeaveWorkflow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeaveWorkflowRepository extends JpaRepository<LeaveWorkflow, Long> {
    List<LeaveWorkflow> findByLeaveRequest_LeaveRequestIdOrderByChangedAtAsc(Long leaveRequestId);
}
