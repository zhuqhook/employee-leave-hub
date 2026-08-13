import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { LeaveTypeService } from '../../services/leave-type.service';
import { LeaveRequestService } from '../../services/leave-request.service';
import { LeaveType, LeaveRequest } from '../../models/models';

@Component({
  selector: 'app-new-request',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <h2>Cerere noua de concediu</h2>

    <div class="card form-card" *ngIf="!created">
      <div class="error-box" *ngIf="error">{{ error }}</div>

      <label>Tip concediu</label>
      <select [(ngModel)]="leaveTypeId">
        <option [ngValue]="null">Selectati...</option>
        <option *ngFor="let t of leaveTypes" [ngValue]="t.leaveTypeId">
          {{ t.name }} ({{ t.code }}){{ t.requiresAttachment ? ' - necesita document' : '' }}
        </option>
      </select>

      <div style="display:flex; gap: 16px;">
        <div style="flex:1;">
          <label>Data inceput</label>
          <input type="date" [(ngModel)]="startDate" />
        </div>
        <div style="flex:1;">
          <label>Data sfarsit</label>
          <input type="date" [(ngModel)]="endDate" />
        </div>
      </div>

      <div style="margin-top: 24px; display:flex; gap: 10px;">
        <button class="btn" (click)="save()" [disabled]="saving">
          {{ saving ? 'Se salveaza...' : 'Salveaza cererea' }}
        </button>
        <button class="btn btn-secondary" (click)="router.navigate(['/my-requests'])">Anuleaza</button>
      </div>
    </div>

    <div class="card form-card" *ngIf="created">
      <div class="success-box">Cererea a fost salvata ca netrimisa. Zile lucratoare calculate: {{ created.workingDays }}.</div>

      <div *ngIf="requiresAttachment()">
        <label>Document justificativ (obligatoriu pentru acest tip de concediu)</label>
        <input type="file" (change)="onFileSelected($event)" />
        <button class="btn btn-secondary" style="margin-top: 8px;" (click)="uploadFile()" [disabled]="!selectedFile || uploading">
          {{ uploading ? 'Se incarca...' : 'Incarca document' }}
        </button>
        <div class="success-box" *ngIf="uploaded">Document incarcat cu succes.</div>
      </div>

      <div class="error-box" *ngIf="submitError">{{ submitError }}</div>

      <div style="margin-top: 20px; display:flex; gap: 10px;">
        <button class="btn" (click)="submitForApproval()" [disabled]="submitting">
          {{ submitting ? 'Se trimite...' : 'Trimite spre aprobare' }}
        </button>
        <button class="btn btn-secondary" (click)="router.navigate(['/my-requests'])">Ramane netrimisa, inapoi la lista</button>
      </div>
    </div>
  `,
  styles: [`
    .form-card { max-width: 500px; }
    h2 { margin-bottom: 16px; }
  `]
})
export class NewRequestComponent implements OnInit {
  leaveTypes: LeaveType[] = [];
  leaveTypeId: number | null = null;
  startDate = '';
  endDate = '';
  error = '';
  saving = false;

  created: LeaveRequest | null = null;
  selectedFile: File | null = null;
  uploading = false;
  uploaded = false;
  submitting = false;
  submitError = '';

  constructor(
    private leaveTypeService: LeaveTypeService,
    private leaveRequestService: LeaveRequestService,
    public router: Router
  ) {}

  ngOnInit(): void {
    this.leaveTypeService.findAll().subscribe((types) => (this.leaveTypes = types));
  }

  save(): void {
    this.error = '';
    if (!this.leaveTypeId || !this.startDate || !this.endDate) {
      this.error = 'Completati tipul de concediu si perioada.';
      return;
    }
    this.saving = true;
    this.leaveRequestService
      .create({ leaveTypeId: this.leaveTypeId, startDate: this.startDate, endDate: this.endDate, submit: false })
      .subscribe({
        next: (req) => {
          this.saving = false;
          this.created = req;
        },
        error: (err) => {
          this.saving = false;
          this.error = err?.error?.message ?? 'Nu s-a putut salva cererea.';
        }
      });
  }

  requiresAttachment(): boolean {
    const type = this.leaveTypes.find((t) => t.leaveTypeId === this.leaveTypeId);
    return !!type?.requiresAttachment;
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedFile = input.files && input.files.length > 0 ? input.files[0] : null;
  }

  uploadFile(): void {
    if (!this.created || !this.selectedFile) {
      return;
    }
    this.uploading = true;
    this.leaveRequestService.addAttachment(this.created.leaveRequestId, this.selectedFile).subscribe({
      next: () => {
        this.uploading = false;
        this.uploaded = true;
      },
      error: (err) => {
        this.uploading = false;
        this.submitError = err?.error?.message ?? 'Nu s-a putut incarca documentul.';
      }
    });
  }

  submitForApproval(): void {
    if (!this.created) {
      return;
    }
    this.submitting = true;
    this.submitError = '';
    this.leaveRequestService.submit(this.created.leaveRequestId).subscribe({
      next: () => {
        this.submitting = false;
        this.router.navigate(['/my-requests']);
      },
      error: (err) => {
        this.submitting = false;
        this.submitError = err?.error?.message ?? 'Nu s-a putut trimite cererea spre aprobare.';
      }
    });
  }
}
