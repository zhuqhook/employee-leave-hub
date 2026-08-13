import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LeaveTypeService } from '../../services/leave-type.service';
import { LeaveType } from '../../models/models';

@Component({
  selector: 'app-admin-leave-types',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <h2>Administrare tipuri de concediu</h2>

    <div class="error-box" *ngIf="error">{{ error }}</div>

    <div class="card form-card">
      <h3>{{ editingId ? 'Editeaza tip concediu' : 'Tip de concediu nou' }}</h3>
      <label>Denumire</label>
      <input [(ngModel)]="form.name" />

      <label>Cod (ex: CO, CM, FP, SPECIAL)</label>
      <input [(ngModel)]="form.code" />

      <label style="display:flex; align-items:center; gap:8px;">
        <input type="checkbox" style="width:auto;" [(ngModel)]="form.requiresAttachment" /> Necesita document atasat
      </label>
      <label style="display:flex; align-items:center; gap:8px;">
        <input type="checkbox" style="width:auto;" [(ngModel)]="form.paid" /> Platit
      </label>

      <div style="margin-top:16px; display:flex; gap:10px;">
        <button class="btn" (click)="save()">{{ editingId ? 'Salveaza modificarile' : 'Adauga tip' }}</button>
        <button class="btn btn-secondary" *ngIf="editingId" (click)="resetForm()">Anuleaza editarea</button>
      </div>
    </div>

    <table class="card" style="margin-top:20px;" *ngIf="leaveTypes.length > 0">
      <thead><tr><th>Denumire</th><th>Cod</th><th>Necesita document</th><th>Platit</th><th></th></tr></thead>
      <tbody>
        <tr *ngFor="let t of leaveTypes">
          <td>{{ t.name }}</td>
          <td>{{ t.code }}</td>
          <td>{{ t.requiresAttachment ? 'Da' : 'Nu' }}</td>
          <td>{{ t.paid ? 'Da' : 'Nu' }}</td>
          <td>
            <button class="btn btn-secondary" (click)="edit(t)">Editeaza</button>
            <button class="btn btn-danger" (click)="remove(t)">Sterge</button>
          </td>
        </tr>
      </tbody>
    </table>
  `,
  styles: [`.form-card { max-width: 480px; } table button { margin-right: 6px; }`]
})
export class AdminLeaveTypesComponent implements OnInit {
  leaveTypes: LeaveType[] = [];
  form: Partial<LeaveType> = { name: '', code: '', requiresAttachment: false, paid: true };
  editingId: number | null = null;
  error = '';

  constructor(private leaveTypeService: LeaveTypeService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.leaveTypeService.findAll().subscribe((t) => (this.leaveTypes = t));
  }

  save(): void {
    this.error = '';
    if (!this.form.name || !this.form.code) {
      this.error = 'Completati denumirea si codul.';
      return;
    }
    const action = this.editingId
      ? this.leaveTypeService.update(this.editingId, this.form)
      : this.leaveTypeService.create(this.form);

    action.subscribe({
      next: () => {
        this.resetForm();
        this.load();
      },
      error: (err) => (this.error = err?.error?.message ?? 'Operatia a esuat.')
    });
  }

  edit(t: LeaveType): void {
    this.editingId = t.leaveTypeId;
    this.form = { name: t.name, code: t.code, requiresAttachment: t.requiresAttachment, paid: t.paid };
  }

  remove(t: LeaveType): void {
    if (!confirm(`Stergeti tipul de concediu ${t.name}?`)) return;
    this.leaveTypeService.delete(t.leaveTypeId).subscribe({
      next: () => this.load(),
      error: (err) => (this.error = err?.error?.message ?? 'Tipul de concediu nu a putut fi sters.')
    });
  }

  resetForm(): void {
    this.editingId = null;
    this.form = { name: '', code: '', requiresAttachment: false, paid: true };
  }
}
