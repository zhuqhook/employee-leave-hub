import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { LeaveRequestService } from '../../services/leave-request.service';
import { ReportService } from '../../services/report.service';
import { AuthService } from '../../services/auth.service';
import { LeaveRequest } from '../../models/models';

@Component({
  selector: 'app-request-detail',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div *ngIf="loading">Se incarca...</div>
    <div class="error-box" *ngIf="error">{{ error }}</div>

    <div *ngIf="request as r">
      <div class="header-row">
        <h2>Cerere #{{ r.leaveRequestId }} - {{ r.employeeName }}</h2>
        <span class="badge" [class]="'badge-' + r.status">{{ statusLabel(r.status) }}</span>
      </div>

      <div class="card">
        <div class="grid">
          <div><label>Departament</label><div>{{ r.departmentName ?? '-' }}</div></div>
          <div><label>Tip concediu</label><div>{{ r.leaveTypeName }} ({{ r.leaveTypeCode }})</div></div>
          <div><label>Perioada</label><div>{{ r.startDate }} &rarr; {{ r.endDate }}</div></div>
          <div><label>Zile lucratoare</label><div>{{ r.workingDays }}</div></div>
          <div><label>Creata la</label><div>{{ r.createdAt | slice:0:16 }}</div></div>
        </div>

        <div style="margin-top:16px;">
          <label>Documente atasate</label>
          <div *ngIf="r.attachments.length === 0">Niciun document atasat.</div>
          <ul>
            <li *ngFor="let a of r.attachments">
              <a href="javascript:void(0)" (click)="downloadAttachment(a.attachmentId, a.fileName)">{{ a.fileName }}</a>
            </li>
          </ul>
        </div>

        <button class="btn btn-secondary" (click)="downloadPdf()">Descarca PDF cerere</button>
      </div>

      <div class="card" style="margin-top:16px;" *ngIf="canCancel()">
        <button class="btn btn-danger" (click)="cancel()">Anuleaza cererea</button>
      </div>

      <div class="card" style="margin-top:16px;" *ngIf="canDecide()">
        <h3>Decizie</h3>
        <label>Comentariu (obligatoriu la respingere)</label>
        <textarea [(ngModel)]="comment" rows="3"></textarea>
        <div class="error-box" *ngIf="decisionError">{{ decisionError }}</div>
        <div style="margin-top:12px; display:flex; gap:10px;">
          <button class="btn" (click)="approve(false)">Aproba</button>
          <button class="btn btn-danger" (click)="reject()">Respinge</button>
        </div>
      </div>

      <div class="card" style="margin-top:16px;">
        <h3>Istoric</h3>
        <table>
          <thead><tr><th>Data</th><th>De la</th><th>La</th><th>Realizat de / Comentariu</th></tr></thead>
          <tbody>
            <tr *ngFor="let h of r.history">
              <td>{{ h.changedAt | slice:0:16 }}</td>
              <td>{{ h.oldStatus ? statusLabel(h.oldStatus) : '-' }}</td>
              <td>{{ statusLabel(h.currentStatus) }}</td>
              <td>{{ h.changedByName }}<span *ngIf="h.comment"> - {{ h.comment }}</span></td>
            </tr>
          </tbody>
        </table>
      </div>

      <button class="btn btn-secondary" style="margin-top:16px;" (click)="back()">Inapoi</button>
    </div>
  `,
  styles: [`
    .header-row { display:flex; align-items:center; gap:16px; margin-bottom:16px; }
    .grid { display:grid; grid-template-columns: repeat(3, 1fr); gap: 14px; }
    .grid label { margin: 0 0 2px; }
    ul { margin: 4px 0; padding-left: 18px; }
  `]
})
export class RequestDetailComponent implements OnInit {
  request: LeaveRequest | null = null;
  loading = true;
  error = '';
  comment = '';
  decisionError = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private leaveRequestService: LeaveRequestService,
    private reportService: ReportService,
    public auth: AuthService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.leaveRequestService.findById(id).subscribe({
      next: (r) => {
        this.request = r;
        this.loading = false;
      },
      error: () => {
        this.error = 'Cererea nu a putut fi incarcata.';
        this.loading = false;
      }
    });
  }

  canCancel(): boolean {
    const r = this.request;
    const me = this.auth.currentEmployee();
    if (!r || !me) return false;
    const isOwner = r.emplId === me.emplId;
    return isOwner && (r.status === 'DRAFT' || r.status === 'PENDING');
  }

  canDecide(): boolean {
    const r = this.request;
    if (!r) return false;
    return this.auth.isManagerOrAdmin() && r.status === 'PENDING';
  }

  approve(override: boolean): void {
    if (!this.request) return;
    this.decisionError = '';
    this.leaveRequestService.approve(this.request.leaveRequestId, this.comment || null, override).subscribe({
      next: (r) => (this.request = r),
      error: (err) => {
        const message: string = err?.error?.message ?? 'Nu s-a putut aproba cererea.';
        if (message.includes('numarul maxim')) {
          if (confirm(message + '\n\nDoriti sa aprobati oricum?')) {
            this.approve(true);
            return;
          }
        }
        this.decisionError = message;
      }
    });
  }

  reject(): void {
    if (!this.request) return;
    if (!this.comment.trim()) {
      this.decisionError = 'Comentariul este obligatoriu la respingere.';
      return;
    }
    this.decisionError = '';
    this.leaveRequestService.reject(this.request.leaveRequestId, this.comment).subscribe({
      next: (r) => (this.request = r),
      error: (err) => (this.decisionError = err?.error?.message ?? 'Nu s-a putut respinge cererea.')
    });
  }

  cancel(): void {
    if (!this.request) return;
    if (!confirm('Sigur doriti sa anulati aceasta cerere?')) return;
    this.leaveRequestService.cancel(this.request.leaveRequestId).subscribe({
      next: (r) => (this.request = r),
      error: (err) => (this.error = err?.error?.message ?? 'Nu s-a putut anula cererea.')
    });
  }

  downloadPdf(): void {
    if (!this.request) return;
    this.reportService.leaveRequestPdf(this.request.leaveRequestId).subscribe((blob) => {
      ReportService.downloadBlob(blob, `cerere-concediu-${this.request!.leaveRequestId}.pdf`);
    });
  }

  downloadAttachment(attachmentId: number, fileName: string): void {
    if (!this.request) return;
    this.leaveRequestService.downloadAttachment(this.request.leaveRequestId, attachmentId).subscribe((blob) => {
      ReportService.downloadBlob(blob, fileName);
    });
  }

  statusLabel(status: string): string {
    const map: Record<string, string> = {
      DRAFT: 'Netrimisa', PENDING: 'In asteptare', APPROVED: 'Aprobata', REJECTED: 'Respinsa', CANCELLED: 'Anulata'
    };
    return map[status] ?? status;
  }

  back(): void {
    this.router.navigate([this.auth.isManagerOrAdmin() ? '/approvals' : '/my-requests']);
  }
}
