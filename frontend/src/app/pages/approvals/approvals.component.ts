import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { LeaveRequestService, LeaveRequestFilters } from '../../services/leave-request.service';
import { ReportService } from '../../services/report.service';
import { DepartmentService } from '../../services/department.service';
import { LeaveTypeService } from '../../services/leave-type.service';
import { AuthService } from '../../services/auth.service';
import { LeaveRequest, Department, LeaveType, LeaveStatus } from '../../models/models';

@Component({
  selector: 'app-approvals',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <h2>Aprobari cereri de concediu</h2>

    <div class="card filters">
      <div>
        <label>Departament</label>
        <select [(ngModel)]="filters.deptId" (ngModelChange)="search()">
          <option [ngValue]="undefined">Toate</option>
          <option *ngFor="let d of departments" [ngValue]="d.deptId">{{ d.departmentName }}</option>
        </select>
      </div>
      <div>
        <label>Status</label>
        <select [(ngModel)]="filters.status" (ngModelChange)="search()">
          <option [ngValue]="undefined">Toate</option>
          <option value="PENDING">In asteptare</option>
          <option value="APPROVED">Aprobate</option>
          <option value="REJECTED">Respinse</option>
          <option value="CANCELLED">Anulate</option>
          <option value="DRAFT">Ciorne</option>
        </select>
      </div>
      <div>
        <label>Tip concediu</label>
        <select [(ngModel)]="filters.leaveTypeId" (ngModelChange)="search()">
          <option [ngValue]="undefined">Toate</option>
          <option *ngFor="let t of leaveTypes" [ngValue]="t.leaveTypeId">{{ t.name }}</option>
        </select>
      </div>
      <div>
        <label>De la</label>
        <input type="date" [(ngModel)]="filters.from" (ngModelChange)="search()" />
      </div>
      <div>
        <label>Pana la</label>
        <input type="date" [(ngModel)]="filters.to" (ngModelChange)="search()" />
      </div>
      <div style="align-self:flex-end;">
        <button class="btn btn-secondary" (click)="downloadReport()">Export PDF</button>
      </div>
    </div>

    <div *ngIf="loading">Se incarca...</div>
    <div class="error-box" *ngIf="error">{{ error }}</div>

    <table class="card" *ngIf="!loading && requests.length > 0">
      <thead>
        <tr>
          <th>Angajat</th><th>Departament</th><th>Tip</th><th>Perioada</th><th>Zile</th><th>Status</th><th></th>
        </tr>
      </thead>
      <tbody>
        <tr *ngFor="let r of requests">
          <td>{{ r.employeeName }}</td>
          <td>{{ r.departmentName ?? '-' }}</td>
          <td>{{ r.leaveTypeCode }}</td>
          <td>{{ r.startDate }} &rarr; {{ r.endDate }}</td>
          <td>{{ r.workingDays }}</td>
          <td><span class="badge" [class]="'badge-' + r.status">{{ statusLabel(r.status) }}</span></td>
          <td><button class="btn btn-secondary" (click)="router.navigate(['/requests', r.leaveRequestId])">Detalii</button></td>
        </tr>
      </tbody>
    </table>

    <div class="card" *ngIf="!loading && requests.length === 0">Nu exista cereri care sa corespunda filtrelor.</div>
  `,
  styles: [`
    .filters { display: flex; gap: 16px; flex-wrap: wrap; margin-bottom: 16px; align-items: flex-start; }
    .filters > div { min-width: 160px; }
    .filters label { margin-top: 0; }
  `]
})
export class ApprovalsComponent implements OnInit {
  requests: LeaveRequest[] = [];
  departments: Department[] = [];
  leaveTypes: LeaveType[] = [];
  filters: LeaveRequestFilters = { status: 'PENDING' as LeaveStatus };
  loading = true;
  error = '';

  constructor(
    private leaveRequestService: LeaveRequestService,
    private reportService: ReportService,
    private departmentService: DepartmentService,
    private leaveTypeService: LeaveTypeService,
    public auth: AuthService,
    public router: Router
  ) {}

  ngOnInit(): void {
    this.departmentService.findAll().subscribe((d) => (this.departments = d));
    this.leaveTypeService.findAll().subscribe((t) => (this.leaveTypes = t));
    this.search();
  }

  search(): void {
    this.loading = true;
    this.leaveRequestService.search(this.filters).subscribe({
      next: (data) => {
        this.requests = data;
        this.loading = false;
      },
      error: () => {
        this.error = 'Nu s-au putut incarca cererile.';
        this.loading = false;
      }
    });
  }

  downloadReport(): void {
    this.reportService.requestsReportPdf(this.filters).subscribe((blob) => {
      ReportService.downloadBlob(blob, 'raport-cereri-concediu.pdf');
    });
  }

  statusLabel(status: string): string {
    const map: Record<string, string> = {
      DRAFT: 'Netrimisa', PENDING: 'In asteptare', APPROVED: 'Aprobata', REJECTED: 'Respinsa', CANCELLED: 'Anulata'
    };
    return map[status] ?? status;
  }
}
