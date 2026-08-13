import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DepartmentService } from '../../services/department.service';
import { EmployeeService } from '../../services/employee.service';
import { Department, Employee } from '../../models/models';

@Component({
  selector: 'app-admin-departments',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <h2>Administrare departamente</h2>

    <div class="error-box" *ngIf="error">{{ error }}</div>

    <div class="card form-card">
      <h3>{{ editingId ? 'Editeaza departament' : 'Departament nou' }}</h3>
      <label>Nume departament</label>
      <input [(ngModel)]="form.departmentName" />

      <label>Responsabil</label>
      <select [(ngModel)]="form.managerId">
        <option [ngValue]="null">Fara responsabil</option>
        <option *ngFor="let e of employees" [ngValue]="e.emplId">{{ e.name }}</option>
      </select>

      <label>Numar maxim de angajati absenti simultan</label>
      <input type="number" min="0" [(ngModel)]="form.maxAbsentEmployees" />

      <div style="margin-top:16px; display:flex; gap:10px;">
        <button class="btn" (click)="save()">{{ editingId ? 'Salveaza modificarile' : 'Adauga departament' }}</button>
        <button class="btn btn-secondary" *ngIf="editingId" (click)="resetForm()">Anuleaza editarea</button>
      </div>
    </div>

    <table class="card" style="margin-top:20px;" *ngIf="departments.length > 0">
      <thead><tr><th>Nume</th><th>Responsabil</th><th>Max absenti simultan</th><th></th></tr></thead>
      <tbody>
        <tr *ngFor="let d of departments">
          <td>{{ d.departmentName }}</td>
          <td>{{ d.managerName ?? '-' }}</td>
          <td>{{ d.maxAbsentEmployees }}</td>
          <td>
            <button class="btn btn-secondary" (click)="edit(d)">Editeaza</button>
            <button class="btn btn-danger" (click)="remove(d)">Sterge</button>
          </td>
        </tr>
      </tbody>
    </table>
  `,
  styles: [`.form-card { max-width: 480px; } table button { margin-right: 6px; }`]
})
export class AdminDepartmentsComponent implements OnInit {
  departments: Department[] = [];
  employees: Employee[] = [];
  form: Partial<Department> = { departmentName: '', managerId: null, maxAbsentEmployees: 1 };
  editingId: number | null = null;
  error = '';

  constructor(private departmentService: DepartmentService, private employeeService: EmployeeService) {}

  ngOnInit(): void {
    this.load();
    this.employeeService.findAll().subscribe((e) => (this.employees = e));
  }

  load(): void {
    this.departmentService.findAll().subscribe((d) => (this.departments = d));
  }

  save(): void {
    this.error = '';
    if (!this.form.departmentName || !this.form.maxAbsentEmployees) {
      this.error = 'Completati numele departamentului si numarul maxim de angajati absenti.';
      return;
    }
    const action = this.editingId
      ? this.departmentService.update(this.editingId, this.form)
      : this.departmentService.create(this.form);

    action.subscribe({
      next: () => {
        this.resetForm();
        this.load();
      },
      error: (err) => (this.error = err?.error?.message ?? 'Operatia a esuat.')
    });
  }

  edit(d: Department): void {
    this.editingId = d.deptId;
    this.form = { departmentName: d.departmentName, managerId: d.managerId, maxAbsentEmployees: d.maxAbsentEmployees };
  }

  remove(d: Department): void {
    if (!confirm(`Stergeti departamentul ${d.departmentName}?`)) return;
    this.departmentService.delete(d.deptId).subscribe({
      next: () => this.load(),
      error: (err) => (this.error = err?.error?.message ?? 'Departamentul nu a putut fi sters.')
    });
  }

  resetForm(): void {
    this.editingId = null;
    this.form = { departmentName: '', managerId: null, maxAbsentEmployees: 1 };
  }
}
