import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { EmployeeService } from '../../services/employee.service';
import { DepartmentService } from '../../services/department.service';
import { ReportService } from '../../services/report.service';
import { Employee, EmployeeCreate, Department, Role } from '../../models/models';

@Component({
  selector: 'app-admin-employees',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="header-row">
      <h2>Administrare angajati</h2>
      <button class="btn btn-secondary" (click)="downloadBalances()">Export situatie solduri (PDF)</button>
    </div>

    <div class="error-box" *ngIf="error">{{ error }}</div>

    <div class="card form-card">
      <h3>{{ editingId ? 'Editeaza angajat' : 'Angajat nou' }}</h3>
      <label>Nume</label>
      <input [(ngModel)]="form.name" />

      <label>Email</label>
      <input type="email" [(ngModel)]="form.email" />

      <label>Parola {{ editingId ? '(lasati gol pentru a pastra parola actuala)' : '' }}</label>
      <input type="password" [(ngModel)]="form.password" />

      <label>Rol</label>
      <select [(ngModel)]="form.role">
        <option value="USER">Angajat</option>
        <option value="DEPARTMENT_MANAGER">Responsabil departament</option>
        <option value="ADMIN">Administrator</option>
      </select>

      <label>Departament</label>
      <select [(ngModel)]="form.deptId">
        <option [ngValue]="null">Fara departament</option>
        <option *ngFor="let d of departments" [ngValue]="d.deptId">{{ d.departmentName }}</option>
      </select>

      <label>Zile concediu anual</label>
      <input type="number" min="0" [(ngModel)]="form.annualLeaveDays" />

      <label style="display:flex; align-items:center; gap:8px;">
        <input type="checkbox" style="width:auto;" [(ngModel)]="form.active" /> Activ
      </label>

      <div style="margin-top:16px; display:flex; gap:10px;">
        <button class="btn" (click)="save()">{{ editingId ? 'Salveaza modificarile' : 'Adauga angajat' }}</button>
        <button class="btn btn-secondary" *ngIf="editingId" (click)="resetForm()">Anuleaza editarea</button>
      </div>
    </div>

    <table class="card" style="margin-top:20px;" *ngIf="employees.length > 0">
      <thead>
        <tr><th>Nume</th><th>Email</th><th>Rol</th><th>Departament</th><th>Sold</th><th>Activ</th><th></th></tr>
      </thead>
      <tbody>
        <tr *ngFor="let e of employees">
          <td>{{ e.name }}</td>
          <td>{{ e.email }}</td>
          <td>{{ roleLabel(e.role) }}</td>
          <td>{{ e.departmentName ?? '-' }}</td>
          <td>{{ e.availableLeaveDays }} / {{ e.annualLeaveDays }}</td>
          <td>{{ e.active ? 'Da' : 'Nu' }}</td>
          <td>
            <button class="btn btn-secondary" (click)="edit(e)">Editeaza</button>
            <button class="btn btn-danger" (click)="remove(e)">Dezactiveaza</button>
          </td>
        </tr>
      </tbody>
    </table>
  `,
  styles: [`
    .header-row { display:flex; justify-content:space-between; align-items:center; margin-bottom:16px; }
    .form-card { max-width: 480px; }
    table button { margin-right: 6px; }
  `]
})
export class AdminEmployeesComponent implements OnInit {
  employees: Employee[] = [];
  departments: Department[] = [];
  form: EmployeeCreate = { name: '', email: '', password: '', role: 'USER', deptId: null, annualLeaveDays: 21, active: true };
  editingId: number | null = null;
  error = '';

  constructor(
    private employeeService: EmployeeService,
    private departmentService: DepartmentService,
    private reportService: ReportService
  ) {}

  ngOnInit(): void {
    this.load();
    this.departmentService.findAll().subscribe((d) => (this.departments = d));
  }

  load(): void {
    this.employeeService.findAll().subscribe((e) => (this.employees = e));
  }

  save(): void {
    this.error = '';
    if (!this.form.name || !this.form.email || (!this.editingId && !this.form.password)) {
      this.error = 'Completati numele, email-ul si parola (pentru angajati noi).';
      return;
    }
    const action = this.editingId
      ? this.employeeService.update(this.editingId, this.form)
      : this.employeeService.create(this.form);

    action.subscribe({
      next: () => {
        this.resetForm();
        this.load();
      },
      error: (err) => (this.error = err?.error?.message ?? 'Operatia a esuat.')
    });
  }

  edit(e: Employee): void {
    this.editingId = e.emplId;
    this.form = {
      name: e.name,
      email: e.email,
      password: '',
      role: e.role,
      deptId: e.deptId,
      annualLeaveDays: e.annualLeaveDays,
      active: e.active
    };
  }

  remove(e: Employee): void {
    if (!confirm(`Dezactivati contul angajatului ${e.name}?`)) return;
    this.employeeService.delete(e.emplId).subscribe({
      next: () => this.load(),
      error: (err) => (this.error = err?.error?.message ?? 'Angajatul nu a putut fi dezactivat.')
    });
  }

  resetForm(): void {
    this.editingId = null;
    this.form = { name: '', email: '', password: '', role: 'USER', deptId: null, annualLeaveDays: 21, active: true };
  }

  roleLabel(role: Role): string {
    const map: Record<Role, string> = { USER: 'Angajat', DEPARTMENT_MANAGER: 'Responsabil departament', ADMIN: 'Administrator' };
    return map[role];
  }

  downloadBalances(): void {
    this.reportService.balancesPdf().subscribe((blob) => {
      ReportService.downloadBlob(blob, 'situatie-solduri.pdf');
    });
  }
}
