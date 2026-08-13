import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { LeaveRequestService } from '../../services/leave-request.service';
import { AuthService } from '../../services/auth.service';
import { LeaveRequest } from '../../models/models';

@Component({
  selector: 'app-my-requests',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="header-row">
      <div>
        <h2>Cererile mele de concediu</h2>
        <p class="balance" *ngIf="auth.currentEmployee() as e">
          Sold disponibil: <strong>{{ e.availableLeaveDays }}</strong> / {{ e.annualLeaveDays }} zile
        </p>
      </div>
      <button class="btn" (click)="router.navigate(['/my-requests/new'])">+ Cerere noua</button>
    </div>

    <div class="card" *ngIf="loading">Se incarca...</div>
    <div class="error-box" *ngIf="error">{{ error }}</div>

    <div class="card" *ngIf="!loading && requests.length === 0">
      Nu aveti nicio cerere de concediu inregistrata.
    </div>

    <table class="card" *ngIf="!loading && requests.length > 0">
      <thead>
        <tr>
          <th>Tip</th>
          <th>Perioada</th>
          <th>Zile</th>
          <th>Status</th>
          <th>Creata la</th>
          <th></th>
        </tr>
      </thead>
      <tbody>
        <tr *ngFor="let r of requests">
          <td>{{ r.leaveTypeName }}</td>
          <td>{{ r.startDate }} &rarr; {{ r.endDate }}</td>
          <td>{{ r.workingDays }}</td>
          <td><span class="badge" [class]="'badge-' + r.status">{{ statusLabel(r.status) }}</span></td>
          <td>{{ r.createdAt | slice:0:10 }}</td>
          <td>
            <button class="btn btn-secondary" (click)="router.navigate(['/requests', r.leaveRequestId])">Detalii</button>
          </td>
        </tr>
      </tbody>
    </table>
  `,
  styles: [`
    .header-row { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 16px; }
    h2 { margin: 0 0 4px; }
    .balance { margin: 0; color: #555; font-size: 14px; }
    table { margin-top: 0; }
  `]
})
export class MyRequestsComponent implements OnInit {
  requests: LeaveRequest[] = [];
  loading = true;
  error = '';

  constructor(private leaveRequestService: LeaveRequestService, public auth: AuthService, public router: Router) {}

  ngOnInit(): void {
    this.leaveRequestService.mine().subscribe({
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

  statusLabel(status: string): string {
    const map: Record<string, string> = {
      DRAFT: 'Netrimisa', PENDING: 'In asteptare', APPROVED: 'Aprobata', REJECTED: 'Respinsa', CANCELLED: 'Anulata'
    };
    return map[status] ?? status;
  }
}
