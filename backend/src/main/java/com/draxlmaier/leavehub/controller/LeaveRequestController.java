package com.draxlmaier.leavehub.controller;

import com.draxlmaier.leavehub.dto.*;
import com.draxlmaier.leavehub.entity.LeaveStatus;
import com.draxlmaier.leavehub.security.EmployeePrincipal;
import com.draxlmaier.leavehub.service.LeaveRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/leave-requests")
@RequiredArgsConstructor
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    @PostMapping
    public LeaveRequestDto create(@AuthenticationPrincipal EmployeePrincipal principal,
                                   @Valid @RequestBody LeaveRequestCreateDto dto) {
        return leaveRequestService.create(principal.getEmployee(), dto);
    }

    @PostMapping(value = "/{id}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AttachmentDto addAttachment(@AuthenticationPrincipal EmployeePrincipal principal,
                                        @PathVariable Long id,
                                        @RequestParam("file") MultipartFile file) {
        return leaveRequestService.addAttachment(id, principal.getEmployee(), file);
    }

    @GetMapping("/{id}/attachments/{attachmentId}")
    public ResponseEntity<byte[]> downloadAttachment(@PathVariable Long id, @PathVariable Long attachmentId) {
        byte[] content = leaveRequestService.downloadAttachment(id, attachmentId);
        String fileName = leaveRequestService.attachmentFileName(attachmentId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(content);
    }

    @PostMapping("/{id}/submit")
    public LeaveRequestDto submit(@AuthenticationPrincipal EmployeePrincipal principal, @PathVariable Long id) {
        return leaveRequestService.submitDto(id, principal.getEmployee());
    }

    @PostMapping("/{id}/cancel")
    public LeaveRequestDto cancel(@AuthenticationPrincipal EmployeePrincipal principal, @PathVariable Long id) {
        return leaveRequestService.cancel(id, principal.getEmployee());
    }

    @GetMapping("/mine")
    public List<LeaveRequestDto> mine(@AuthenticationPrincipal EmployeePrincipal principal) {
        return leaveRequestService.findMine(principal.getEmployee().getEmplId());
    }

    @PreAuthorize("hasAnyRole('ADMIN','DEPARTMENT_MANAGER')")
    @PostMapping("/{id}/approve")
    public LeaveRequestDto approve(@AuthenticationPrincipal EmployeePrincipal principal,
                                    @PathVariable Long id,
                                    @RequestParam(defaultValue = "false") boolean override,
                                    @RequestBody(required = false) LeaveDecisionDto decision) {
        return leaveRequestService.approve(id, principal.getEmployee(), decision, override);
    }

    @PreAuthorize("hasAnyRole('ADMIN','DEPARTMENT_MANAGER')")
    @PostMapping("/{id}/reject")
    public LeaveRequestDto reject(@AuthenticationPrincipal EmployeePrincipal principal,
                                   @PathVariable Long id,
                                   @Valid @RequestBody LeaveDecisionDto decision) {
        return leaveRequestService.reject(id, principal.getEmployee(), decision);
    }

    @PreAuthorize("hasAnyRole('ADMIN','DEPARTMENT_MANAGER')")
    @GetMapping
    public List<LeaveRequestDto> search(@RequestParam(required = false) Long deptId,
                                         @RequestParam(required = false) LeaveStatus status,
                                         @RequestParam(required = false) Long leaveTypeId,
                                         @RequestParam(required = false) Long emplId,
                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return leaveRequestService.search(deptId, status, leaveTypeId, emplId, from, to);
    }

    @GetMapping("/{id}")
    public LeaveRequestDto findById(@PathVariable Long id) {
        return leaveRequestService.findById(id);
    }

    @GetMapping("/calendar")
    public List<CalendarEntryDto> calendar(@RequestParam Long deptId,
                                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return leaveRequestService.calendar(deptId, from, to);
    }
}
