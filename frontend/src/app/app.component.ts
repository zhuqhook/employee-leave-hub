import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterOutlet } from '@angular/router';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet],
  template: `
    <div class="shell" *ngIf="auth.isLoggedIn(); else onlyRouter">
      <header class="topbar">
        <div class="brand">Employee Leave Hub</div>
        <nav>
          <a (click)="go('/my-requests')">Cererile mele</a>
          <a (click)="go('/calendar')">Calendar</a>
          <a *ngIf="auth.isManagerOrAdmin()" (click)="go('/approvals')">Aprobari</a>
          <a *ngIf="auth.isAdmin()" (click)="go('/admin/employees')">Angajati</a>
          <a *ngIf="auth.isAdmin()" (click)="go('/admin/departments')">Departamente</a>
          <a *ngIf="auth.isAdmin()" (click)="go('/admin/leave-types')">Tipuri concediu</a>
        </nav>
        <div class="user-box">
          <span>{{ auth.currentEmployee()?.name }} &middot; {{ roleLabel() }}</span>
          <button class="btn btn-secondary" (click)="logout()">Iesire</button>
        </div>
      </header>
      <main class="content">
        <router-outlet></router-outlet>
      </main>
    </div>
    <ng-template #onlyRouter>
      <router-outlet></router-outlet>
    </ng-template>
  `,
  styles: [`
    .shell { display: flex; flex-direction: column; min-height: 100vh; }
    .topbar {
      display: flex; align-items: center; gap: 24px;
      background: #fff; border-bottom: 1px solid #dfe3e5;
      padding: 12px 24px;
    }
    .brand { font-weight: 700; color: #0096a8; font-size: 18px; white-space: nowrap; }
    nav { display: flex; gap: 18px; flex: 1; }
    nav a { cursor: pointer; font-size: 14px; font-weight: 600; color: #333; text-decoration: none; }
    nav a:hover { color: #0096a8; }
    .user-box { display: flex; align-items: center; gap: 12px; font-size: 13px; color: #555; }
    .content { flex: 1; padding: 24px; max-width: 1200px; margin: 0 auto; width: 100%; }
  `]
})
export class AppComponent {
  constructor(public auth: AuthService, private router: Router) {}

  go(path: string): void {
    this.router.navigate([path]);
  }

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }

  roleLabel(): string {
    const map: Record<string, string> = {
      USER: 'Angajat',
      DEPARTMENT_MANAGER: 'Responsabil departament',
      ADMIN: 'Administrator'
    };
    return map[this.auth.role() ?? ''] ?? '';
  }
}
