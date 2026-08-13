package com.draxlmaier.leavehub.repository;

import com.draxlmaier.leavehub.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findByLeaveRequest_LeaveRequestId(Long leaveRequestId);
}
