import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LeaveRequestService } from '../../services/leave-request.service';
import { DepartmentService } from '../../services/department.service';
import { AuthService } from '../../services/auth.service';
import { CalendarEntry, Department } from '../../models/models';

interface DayCell {
  date: string;
  day: number;
  entries: CalendarEntry[];
}

@Component({
  selector: 'app-calendar',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <h2>Calendar comun al departamentului</h2>

    <div class="card filters">
      <div>
        <label>Departament</label>
        <select [(ngModel)]="deptId" (ngModelChange)="load()">
          <option *ngFor="let d of departments" [ngValue]="d.deptId">{{ d.departmentName }}</option>
        </select>
      </div>
      <div>
        <label>Luna</label>
        <input type="month" [(ngModel)]="month" (ngModelChange)="load()" />
      </div>
    </div>

    <div class="error-box" *ngIf="warningDays.length > 0">
      Atentie: numarul maxim de angajati absenti simultan ({{ maxAbsent }}) este depasit in zilele:
      {{ warningDays.join(', ') }}
    </div>

    <div class="card" *ngIf="!loading">
      <div class="grid">
        <div class="day-cell" *ngFor="let cell of days">
          <div class="day-number" [class.over]="cell.entries.length > maxAbsent">{{ cell.day }}</div>
          <div class="chip" *ngFor="let e of cell.entries" [title]="e.employeeName">
            {{ e.employeeName }} ({{ e.leaveTypeCode }})
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .filters { display:flex; gap:16px; margin-bottom:16px; }
    .grid { display:grid; grid-template-columns: repeat(7, 1fr); gap: 6px; }
    .day-cell { border: 1px solid #dfe3e5; border-radius: 4px; min-height: 90px; padding: 6px; font-size: 12px; }
    .day-number { font-weight: 700; margin-bottom: 4px; }
    .day-number.over { color: #c0392b; }
    .chip { background: #eafcff; border: 1px solid #0096a8; border-radius: 3px; padding: 2px 4px; margin-bottom: 3px; font-size: 11px; }
  `]
})
export class CalendarComponent implements OnInit {
  departments: Department[] = [];
  deptId: number | null = null;
  month = new Date().toISOString().slice(0, 7);
  days: DayCell[] = [];
  maxAbsent = 0;
  warningDays: number[] = [];
  loading = true;

  constructor(
    private leaveRequestService: LeaveRequestService,
    private departmentService: DepartmentService,
    public auth: AuthService
  ) {}

  ngOnInit(): void {
    this.departmentService.findAll().subscribe((depts) => {
      this.departments = depts;
      const me = this.auth.currentEmployee();
      this.deptId = me?.deptId ?? depts[0]?.deptId ?? null;
      if (this.deptId) {
        const dept = depts.find((d) => d.deptId === this.deptId);
        this.maxAbsent = dept?.maxAbsentEmployees ?? 0;
        this.load();
      } else {
        this.loading = false;
      }
    });
  }

  load(): void {
    if (!this.deptId) return;
    this.loading = true;
    const dept = this.departments.find((d) => d.deptId === this.deptId);
    this.maxAbsent = dept?.maxAbsentEmployees ?? 0;

    const [year, monthNum] = this.month.split('-').map(Number);
    const from = new Date(year, monthNum - 1, 1);
    const to = new Date(year, monthNum, 0);
    const fromStr = this.formatDate(from);
    const toStr = this.formatDate(to);

    this.leaveRequestService.calendar(this.deptId, fromStr, toStr).subscribe({
      next: (entries) => {
        this.buildGrid(from, to, entries);
        this.loading = false;
      },
      error: () => (this.loading = false)
    });
  }

  private buildGrid(from: Date, to: Date, entries: CalendarEntry[]): void {
    const cells: DayCell[] = [];
    this.warningDays = [];
    for (let d = new Date(from); d <= to; d.setDate(d.getDate() + 1)) {
      const dateStr = this.formatDate(d);
      const dayEntries = entries.filter((e) => e.startDate <= dateStr && e.endDate >= dateStr);
      if (dayEntries.length > this.maxAbsent) {
        this.warningDays.push(d.getDate());
      }
      cells.push({ date: dateStr, day: d.getDate(), entries: dayEntries });
    }
    this.days = cells;
  }

  private formatDate(d: Date): string {
    return d.toISOString().slice(0, 10);
  }
}
