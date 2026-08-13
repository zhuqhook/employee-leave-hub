package com.draxlmaier.leavehub.service;

import com.draxlmaier.leavehub.dto.*;
import com.draxlmaier.leavehub.entity.*;
import com.draxlmaier.leavehub.exception.BusinessException;
import com.draxlmaier.leavehub.exception.ResourceNotFoundException;
import com.draxlmaier.leavehub.repository.*;
import com.draxlmaier.leavehub.util.WorkingDaysCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveRequestService {

    private static final String PAID_LEAVE_CODE = "CO";

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveWorkflowRepository leaveWorkflowRepository;
    private final AttachmentRepository attachmentRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public LeaveRequestDto create(Employee requester, LeaveRequestCreateDto dto) {
        LeaveType leaveType = leaveTypeRepository.findById(dto.getLeaveTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Tipul de concediu nu a fost gasit."));

        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new BusinessException("Data de sfarsit trebuie sa fie dupa data de inceput.");
        }

        int workingDays = WorkingDaysCalculator.calculate(dto.getStartDate(), dto.getEndDate());
        if (workingDays == 0) {
            throw new BusinessException("Intervalul selectat nu contine nicio zi lucratoare.");
        }

        if (isPaidAnnualLeave(leaveType) && workingDays > requester.getAvailableLeaveDays()) {
            throw new BusinessException("Sold insuficient: disponibil " + requester.getAvailableLeaveDays()
                    + " zile, solicitat " + workingDays + " zile.");
        }

        LeaveRequest request = LeaveRequest.builder()
                .employee(requester)
                .leaveType(leaveType)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .workingDays(workingDays)
                .status(LeaveStatus.DRAFT)
                .createdAt(LocalDateTime.now())
                .build();

        request = leaveRequestRepository.save(request);
        logTransition(request, requester, null, LeaveStatus.DRAFT, "Cerere creata.");

        if (dto.isSubmit()) {
            request = submit(request.getLeaveRequestId(), requester);
        }

        return toDto(leaveRequestRepository.save(request));
    }

    @Transactional
    public AttachmentDto addAttachment(Long requestId, Employee requester, MultipartFile file) {
        LeaveRequest request = getOrThrow(requestId);
        assertOwner(request, requester);

        if (request.getStatus() != LeaveStatus.DRAFT && request.getStatus() != LeaveStatus.PENDING) {
            throw new BusinessException("Documentele pot fi atasate doar cat timp cererea nu a fost procesata.");
        }
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Fisierul atasat este gol.");
        }

        String path = fileStorageService.store(requestId, file);

        Attachment attachment = Attachment.builder()
                .leaveRequest(request)
                .fileName(file.getOriginalFilename())
                .filePath(path)
                .uploadedAt(LocalDateTime.now())
                .build();

        attachment = attachmentRepository.save(attachment);

        return AttachmentDto.builder()
                .attachmentId(attachment.getAttachmentId())
                .fileName(attachment.getFileName())
                .uploadedAt(attachment.getUploadedAt())
                .build();
    }

    public byte[] downloadAttachment(Long requestId, Long attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Documentul nu a fost gasit."));
        if (!attachment.getLeaveRequest().getLeaveRequestId().equals(requestId)) {
            throw new ResourceNotFoundException("Documentul nu apartine acestei cereri.");
        }
        return fileStorageService.read(attachment.getFilePath());
    }

    public String attachmentFileName(Long attachmentId) {
        return attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Documentul nu a fost gasit."))
                .getFileName();
    }

    @Transactional
    public LeaveRequestDto submitDto(Long requestId, Employee requester) {
        return toDto(submit(requestId, requester));
    }

    private LeaveRequest submit(Long requestId, Employee requester) {
        LeaveRequest request = getOrThrow(requestId);
        assertOwner(request, requester);

        if (request.getStatus() != LeaveStatus.DRAFT) {
            throw new BusinessException("Doar cererile in starea DRAFT pot fi trimise spre aprobare.");
        }
        if (request.getLeaveType().isRequiresAttachment() && request.getAttachments().isEmpty()) {
            throw new BusinessException("Tipul de concediu selectat necesita un document atasat.");
        }

        LeaveStatus old = request.getStatus();
        request.setStatus(LeaveStatus.PENDING);
        request = leaveRequestRepository.save(request);
        logTransition(request, requester, old, LeaveStatus.PENDING, "Cerere trimisa spre aprobare.");
        return request;
    }

    @Transactional
    public LeaveRequestDto approve(Long requestId, Employee approver, LeaveDecisionDto decision, boolean overrideCapacity) {
        LeaveRequest request = getOrThrow(requestId);
        assertApprover(request, approver);

        if (request.getStatus() != LeaveStatus.PENDING) {
            throw new BusinessException("Doar cererile in starea PENDING pot fi aprobate.");
        }

        Department department = request.getEmployee().getDepartment();
        if (department != null && !overrideCapacity) {
            List<LeaveRequest> overlapping = leaveRequestRepository.findApprovedOverlapping(
                    department.getDeptId(), request.getStartDate(), request.getEndDate());
            long distinctEmployeesAbsent = overlapping.stream()
                    .map(r -> r.getEmployee().getEmplId())
                    .distinct()
                    .count() + 1; // +1 pentru angajatul curent

            if (distinctEmployeesAbsent > department.getMaxAbsentEmployees()) {
                String names = overlapping.stream()
                        .map(r -> r.getEmployee().getName())
                        .distinct()
                        .collect(Collectors.joining(", "));
                throw new BusinessException("Atentie: aprobarea acestei cereri depaseste numarul maxim de "
                        + department.getMaxAbsentEmployees() + " angajati absenti simultan in departament "
                        + "(deja absenti in perioada: " + names + "). Reincercati cu confirmare explicita daca doriti sa continuati.");
            }
        }

        if (isPaidAnnualLeave(request.getLeaveType())) {
            Employee employee = request.getEmployee();
            if (request.getWorkingDays() > employee.getAvailableLeaveDays()) {
                throw new BusinessException("Sold insuficient pentru angajat la momentul aprobarii.");
            }
            employee.setAvailableLeaveDays(employee.getAvailableLeaveDays() - request.getWorkingDays());
            employeeRepository.save(employee);
        }

        LeaveStatus old = request.getStatus();
        request.setStatus(LeaveStatus.APPROVED);
        request = leaveRequestRepository.save(request);
        logTransition(request, approver, old, LeaveStatus.APPROVED, decision != null ? decision.getComment() : null);
        return toDto(request);
    }

    @Transactional
    public LeaveRequestDto reject(Long requestId, Employee approver, LeaveDecisionDto decision) {
        LeaveRequest request = getOrThrow(requestId);
        assertApprover(request, approver);

        if (request.getStatus() != LeaveStatus.PENDING) {
            throw new BusinessException("Doar cererile in starea PENDING pot fi respinse.");
        }
        if (decision == null || decision.getComment() == null || decision.getComment().isBlank()) {
            throw new BusinessException("Comentariul este obligatoriu la respingerea unei cereri.");
        }

        LeaveStatus old = request.getStatus();
        request.setStatus(LeaveStatus.REJECTED);
        request = leaveRequestRepository.save(request);
        logTransition(request, approver, old, LeaveStatus.REJECTED, decision.getComment());
        return toDto(request);
    }

    @Transactional
    public LeaveRequestDto cancel(Long requestId, Employee requester) {
        LeaveRequest request = getOrThrow(requestId);
        assertOwner(request, requester);

        if (request.getStatus() != LeaveStatus.DRAFT && request.getStatus() != LeaveStatus.PENDING) {
            throw new BusinessException("O cerere poate fi anulata doar cat timp nu a fost aprobata.");
        }

        LeaveStatus old = request.getStatus();
        request.setStatus(LeaveStatus.CANCELLED);
        request = leaveRequestRepository.save(request);
        logTransition(request, requester, old, LeaveStatus.CANCELLED, "Cerere anulata de angajat.");
        return toDto(request);
    }

    public LeaveRequestDto findById(Long id) {
        return toDto(getOrThrow(id));
    }

    public List<LeaveRequestDto> findMine(Long emplId) {
        return leaveRequestRepository.findByEmployee_EmplIdOrderByCreatedAtDesc(emplId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<LeaveRequestDto> search(Long deptId, LeaveStatus status, Long leaveTypeId, Long emplId,
                                         LocalDate from, LocalDate to) {
        return leaveRequestRepository.search(deptId, status, leaveTypeId, emplId, from, to)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<CalendarEntryDto> calendar(Long deptId, LocalDate from, LocalDate to) {
        return leaveRequestRepository.findApprovedOverlapping(deptId, from, to).stream()
                .map(r -> CalendarEntryDto.builder()
                        .leaveRequestId(r.getLeaveRequestId())
                        .emplId(r.getEmployee().getEmplId())
                        .employeeName(r.getEmployee().getName())
                        .leaveTypeCode(r.getLeaveType().getCode())
                        .startDate(r.getStartDate())
                        .endDate(r.getEndDate())
                        .build())
                .collect(Collectors.toList());
    }

    public DepartmentStatsDto stats(Department department, List<LeaveRequest> requests, int employeeCount) {
        long pending = requests.stream().filter(r -> r.getStatus() == LeaveStatus.PENDING).count();
        long approved = requests.stream().filter(r -> r.getStatus() == LeaveStatus.APPROVED).count();
        long rejected = requests.stream().filter(r -> r.getStatus() == LeaveStatus.REJECTED).count();
        long totalDays = requests.stream()
                .filter(r -> r.getStatus() == LeaveStatus.APPROVED)
                .mapToLong(LeaveRequest::getWorkingDays)
                .sum();

        return DepartmentStatsDto.builder()
                .deptId(department.getDeptId())
                .departmentName(department.getDepartmentName())
                .totalRequests(requests.size())
                .pendingRequests(pending)
                .approvedRequests(approved)
                .rejectedRequests(rejected)
                .totalDaysConsumed(totalDays)
                .employeeCount(employeeCount)
                .build();
    }

    public LeaveRequest getOrThrow(Long id) {
        return leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cererea cu id-ul " + id + " nu a fost gasita."));
    }

    private boolean isPaidAnnualLeave(LeaveType type) {
        return PAID_LEAVE_CODE.equalsIgnoreCase(type.getCode());
    }

    private void assertOwner(LeaveRequest request, Employee requester) {
        boolean isOwner = request.getEmployee().getEmplId().equals(requester.getEmplId());
        boolean isAdmin = requester.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new BusinessException("Nu puteti modifica o cerere care nu va apartine.");
        }
    }

    private void assertApprover(LeaveRequest request, Employee approver) {
        if (approver.getRole() == Role.ADMIN) {
            return;
        }
        boolean isDeptManager = approver.getRole() == Role.DEPARTMENT_MANAGER
                && request.getEmployee().getDepartment() != null
                && request.getEmployee().getDepartment().getManager() != null
                && request.getEmployee().getDepartment().getManager().getEmplId().equals(approver.getEmplId());
        if (!isDeptManager) {
            throw new BusinessException("Nu aveti drepturi de aprobare pentru cererile acestui departament.");
        }
    }

    private void logTransition(LeaveRequest request, Employee changedBy, LeaveStatus old, LeaveStatus current, String comment) {
        LeaveWorkflow workflow = LeaveWorkflow.builder()
                .leaveRequest(request)
                .changedBy(changedBy)
                .oldStatus(old)
                .currentStatus(current)
                .changedAt(LocalDateTime.now())
                .comment(comment)
                .build();
        leaveWorkflowRepository.save(workflow);
    }

    public LeaveRequestDto toDto(LeaveRequest r) {
        List<AttachmentDto> attachments = r.getAttachments().stream()
                .map(a -> AttachmentDto.builder()
                        .attachmentId(a.getAttachmentId())
                        .fileName(a.getFileName())
                        .uploadedAt(a.getUploadedAt())
                        .build())
                .collect(Collectors.toList());

        List<WorkflowHistoryDto> history = leaveWorkflowRepository
                .findByLeaveRequest_LeaveRequestIdOrderByChangedAtAsc(r.getLeaveRequestId())
                .stream()
                .map(w -> WorkflowHistoryDto.builder()
                        .oldStatus(w.getOldStatus())
                        .currentStatus(w.getCurrentStatus())
                        .changedByName(w.getChangedBy().getName())
                        .changedAt(w.getChangedAt())
                        .comment(w.getComment())
                        .build())
                .collect(Collectors.toList());

        Department dept = r.getEmployee().getDepartment();

        return LeaveRequestDto.builder()
                .leaveRequestId(r.getLeaveRequestId())
                .emplId(r.getEmployee().getEmplId())
                .employeeName(r.getEmployee().getName())
                .deptId(dept != null ? dept.getDeptId() : null)
                .departmentName(dept != null ? dept.getDepartmentName() : null)
                .leaveTypeId(r.getLeaveType().getLeaveTypeId())
                .leaveTypeName(r.getLeaveType().getName())
                .leaveTypeCode(r.getLeaveType().getCode())
                .startDate(r.getStartDate())
                .endDate(r.getEndDate())
                .workingDays(r.getWorkingDays())
                .status(r.getStatus())
                .createdAt(r.getCreatedAt())
                .attachments(attachments)
                .history(history)
                .build();
    }
}
