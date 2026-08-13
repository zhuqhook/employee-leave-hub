package com.draxlmaier.leavehub.controller;

import com.draxlmaier.leavehub.dto.DepartmentStatsDto;
import com.draxlmaier.leavehub.dto.EmployeeDto;
import com.draxlmaier.leavehub.dto.LeaveRequestDto;
import com.draxlmaier.leavehub.entity.Department;
import com.draxlmaier.leavehub.entity.LeaveRequest;
import com.draxlmaier.leavehub.entity.LeaveStatus;
import com.draxlmaier.leavehub.repository.DepartmentRepository;
import com.draxlmaier.leavehub.repository.EmployeeRepository;
import com.draxlmaier.leavehub.repository.LeaveRequestRepository;
import com.draxlmaier.leavehub.service.EmployeeService;
import com.draxlmaier.leavehub.service.LeaveRequestService;
import com.draxlmaier.leavehub.service.PdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final LeaveRequestService leaveRequestService;
    private final EmployeeService employeeService;
    private final PdfService pdfService;
    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    @GetMapping(value = "/leave-requests/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> leaveRequestPdf(@PathVariable Long id) {
        LeaveRequestDto dto = leaveRequestService.findById(id);
        byte[] pdf = pdfService.generateLeaveRequestPdf(dto);
        return pdfResponse(pdf, "cerere-concediu-" + id + ".pdf");
    }

    @PreAuthorize("hasAnyRole('ADMIN','DEPARTMENT_MANAGER')")
    @GetMapping(value = "/leave-requests/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> requestsReportPdf(@RequestParam(required = false) Long deptId,
                                                      @RequestParam(required = false) LeaveStatus status,
                                                      @RequestParam(required = false) Long leaveTypeId,
                                                      @RequestParam(required = false) Long emplId,
                                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        List<LeaveRequestDto> requests = leaveRequestService.search(deptId, status, leaveTypeId, emplId, from, to);
        String title = "Raport cereri de concediu" + (status != null ? " - " + status : "");
        byte[] pdf = pdfService.generateRequestsReportPdf(title, requests);
        return pdfResponse(pdf, "raport-cereri-concediu.pdf");
    }

    @PreAuthorize("hasAnyRole('ADMIN','DEPARTMENT_MANAGER')")
    @GetMapping(value = "/balances/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> balancesReportPdf(@RequestParam(required = false) Long deptId) {
        List<EmployeeDto> employees = employeeService.findAll().stream()
                .filter(e -> deptId == null || deptId.equals(e.getDeptId()))
                .collect(Collectors.toList());
        byte[] pdf = pdfService.generateBalancesReportPdf("Situatia soldurilor de concediu", employees);
        return pdfResponse(pdf, "situatie-solduri.pdf");
    }

    @PreAuthorize("hasAnyRole('ADMIN','DEPARTMENT_MANAGER')")
    @GetMapping("/departments/{deptId}/stats")
    public DepartmentStatsDto departmentStats(@PathVariable Long deptId) {
        Department department = departmentRepository.findById(deptId)
                .orElseThrow(() -> new com.draxlmaier.leavehub.exception.ResourceNotFoundException(
                        "Departamentul cu id-ul " + deptId + " nu a fost gasit."));
        List<LeaveRequest> requests = leaveRequestRepository.findByEmployee_Department_DeptIdOrderByStartDateDesc(deptId);
        int employeeCount = employeeRepository.findByDepartment_DeptId(deptId).size();
        return leaveRequestService.stats(department, requests, employeeCount);
    }

    private ResponseEntity<byte[]> pdfResponse(byte[] content, String fileName) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(content);
    }
}
