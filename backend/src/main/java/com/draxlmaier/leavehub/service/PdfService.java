package com.draxlmaier.leavehub.service;

import com.draxlmaier.leavehub.dto.EmployeeDto;
import com.draxlmaier.leavehub.dto.LeaveRequestDto;
import com.draxlmaier.leavehub.dto.WorkflowHistoryDto;
import com.draxlmaier.leavehub.exception.BusinessException;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
    private static final Font SECTION_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);
    private static final Font LABEL_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    private static final Font TEXT_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10);
    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);

    public byte[] generateLeaveRequestPdf(LeaveRequestDto r) {
        Document document = new Document(PageSize.A4, 40, 40, 50, 40);
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, out);
            document.open();

            document.add(new Paragraph("Cerere de concediu", TITLE_FONT));
            document.add(new Paragraph("Employee Leave Hub - DRAXLMAIER", TEXT_FONT));
            document.add(Chunk.NEWLINE);

            PdfPTable info = new PdfPTable(2);
            info.setWidthPercentage(100);
            info.setWidths(new float[]{1, 2});
            addRow(info, "Angajat", r.getEmployeeName());
            addRow(info, "Departament", r.getDepartmentName() != null ? r.getDepartmentName() : "-");
            addRow(info, "Tip concediu", r.getLeaveTypeName() + " (" + r.getLeaveTypeCode() + ")");
            addRow(info, "Perioada", r.getStartDate().format(DATE_FMT) + " - " + r.getEndDate().format(DATE_FMT));
            addRow(info, "Zile lucratoare", String.valueOf(r.getWorkingDays()));
            addRow(info, "Status", statusLabel(r.getStatus().name()));
            addRow(info, "Data creare", r.getCreatedAt().format(DATETIME_FMT));
            document.add(info);

            document.add(Chunk.NEWLINE);
            document.add(new Paragraph("Istoric aprobari", SECTION_FONT));
            document.add(Chunk.NEWLINE);

            PdfPTable history = new PdfPTable(4);
            history.setWidthPercentage(100);
            history.setWidths(new float[]{2, 2, 2, 4});
            addHeaderCell(history, "Data");
            addHeaderCell(history, "De la statusul");
            addHeaderCell(history, "La statusul");
            addHeaderCell(history, "Realizat de / Comentariu");

            List<WorkflowHistoryDto> hist = r.getHistory();
            if (hist == null || hist.isEmpty()) {
                PdfPCell empty = new PdfPCell(new Phrase("Fara evenimente inregistrate.", TEXT_FONT));
                empty.setColspan(4);
                history.addCell(empty);
            } else {
                for (WorkflowHistoryDto w : hist) {
                    history.addCell(new PdfPCell(new Phrase(w.getChangedAt().format(DATETIME_FMT), TEXT_FONT)));
                    history.addCell(new PdfPCell(new Phrase(w.getOldStatus() != null ? statusLabel(w.getOldStatus().name()) : "-", TEXT_FONT)));
                    history.addCell(new PdfPCell(new Phrase(statusLabel(w.getCurrentStatus().name()), TEXT_FONT)));
                    String detail = w.getChangedByName() + (w.getComment() != null && !w.getComment().isBlank() ? " - " + w.getComment() : "");
                    history.addCell(new PdfPCell(new Phrase(detail, TEXT_FONT)));
                }
            }
            document.add(history);

            document.close();
            return out.toByteArray();
        } catch (DocumentException e) {
            throw new BusinessException("Eroare la generarea PDF-ului: " + e.getMessage());
        }
    }

    public byte[] generateRequestsReportPdf(String title, List<LeaveRequestDto> requests) {
        Document document = new Document(PageSize.A4.rotate(), 30, 30, 50, 40);
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, out);
            document.open();

            document.add(new Paragraph(title, TITLE_FONT));
            document.add(new Paragraph("Generat la: " + java.time.LocalDateTime.now().format(DATETIME_FMT), TEXT_FONT));
            document.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2, 2, 1.5f, 1.5f, 1.5f, 1, 1.5f});
            addHeaderCell(table, "Angajat");
            addHeaderCell(table, "Departament");
            addHeaderCell(table, "Tip concediu");
            addHeaderCell(table, "Inceput");
            addHeaderCell(table, "Sfarsit");
            addHeaderCell(table, "Zile");
            addHeaderCell(table, "Status");

            if (requests.isEmpty()) {
                PdfPCell empty = new PdfPCell(new Phrase("Nu exista cereri care sa corespunda criteriilor.", TEXT_FONT));
                empty.setColspan(7);
                table.addCell(empty);
            } else {
                for (LeaveRequestDto r : requests) {
                    table.addCell(new PdfPCell(new Phrase(r.getEmployeeName(), TEXT_FONT)));
                    table.addCell(new PdfPCell(new Phrase(r.getDepartmentName() != null ? r.getDepartmentName() : "-", TEXT_FONT)));
                    table.addCell(new PdfPCell(new Phrase(r.getLeaveTypeCode(), TEXT_FONT)));
                    table.addCell(new PdfPCell(new Phrase(r.getStartDate().format(DATE_FMT), TEXT_FONT)));
                    table.addCell(new PdfPCell(new Phrase(r.getEndDate().format(DATE_FMT), TEXT_FONT)));
                    table.addCell(new PdfPCell(new Phrase(String.valueOf(r.getWorkingDays()), TEXT_FONT)));
                    table.addCell(new PdfPCell(new Phrase(statusLabel(r.getStatus().name()), TEXT_FONT)));
                }
            }
            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (DocumentException e) {
            throw new BusinessException("Eroare la generarea PDF-ului: " + e.getMessage());
        }
    }

    public byte[] generateBalancesReportPdf(String title, List<EmployeeDto> employees) {
        Document document = new Document(PageSize.A4, 40, 40, 50, 40);
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, out);
            document.open();

            document.add(new Paragraph(title, TITLE_FONT));
            document.add(new Paragraph("Generat la: " + java.time.LocalDateTime.now().format(DATETIME_FMT), TEXT_FONT));
            document.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2.5f, 2, 1.5f, 1.5f, 1.5f});
            addHeaderCell(table, "Angajat");
            addHeaderCell(table, "Departament");
            addHeaderCell(table, "Sold anual");
            addHeaderCell(table, "Sold disponibil");
            addHeaderCell(table, "Zile consumate");

            for (EmployeeDto e : employees) {
                int consumed = e.getAnnualLeaveDays() - e.getAvailableLeaveDays();
                table.addCell(new PdfPCell(new Phrase(e.getName(), TEXT_FONT)));
                table.addCell(new PdfPCell(new Phrase(e.getDepartmentName() != null ? e.getDepartmentName() : "-", TEXT_FONT)));
                table.addCell(new PdfPCell(new Phrase(String.valueOf(e.getAnnualLeaveDays()), TEXT_FONT)));
                table.addCell(new PdfPCell(new Phrase(String.valueOf(e.getAvailableLeaveDays()), TEXT_FONT)));
                table.addCell(new PdfPCell(new Phrase(String.valueOf(consumed), TEXT_FONT)));
            }
            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (DocumentException e) {
            throw new BusinessException("Eroare la generarea PDF-ului: " + e.getMessage());
        }
    }

    private void addRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, LABEL_FONT));
        labelCell.setBorder(Rectangle.NO_BORDER);
        PdfPCell valueCell = new PdfPCell(new Phrase(value, TEXT_FONT));
        valueCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, HEADER_FONT));
        cell.setBackgroundColor(new Color(0, 150, 168)); // teal DRAXLMAIER-like
        cell.setPadding(5);
        table.addCell(cell);
    }

    private String statusLabel(String status) {
        switch (status) {
            case "DRAFT": return "Netrimisa";
            case "PENDING": return "In asteptare";
            case "APPROVED": return "Aprobata";
            case "REJECTED": return "Respinsa";
            case "CANCELLED": return "Anulata";
            default: return status;
        }
    }
}
