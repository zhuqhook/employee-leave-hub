import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="login-wrap">
      <form class="card login-card" (ngSubmit)="submit()">
        <h1>Employee Leave Hub</h1>
        <p class="subtitle">Autentificare</p>

        <div class="error-box" *ngIf="error">{{ error }}</div>

        <label>Email</label>
        <input type="email" name="email" [(ngModel)]="email" required autocomplete="username" />

        <label>Parola</label>
        <input type="password" name="password" [(ngModel)]="password" required autocomplete="current-password" />

        <button class="btn" type="submit" [disabled]="loading" style="margin-top: 20px; width: 100%;">
          {{ loading ? 'Se autentifica...' : 'Autentificare' }}
        </button>

        <p class="hint">
          Cont demo admin: admin&#64;draxlmaier.com / Admin123!<br>
          Cont demo responsabil: manager.it&#64;draxlmaier.com / Manager123!<br>
          Cont demo angajat: maria.ionescu&#64;draxlmaier.com / User123!
        </p>
      </form>
    </div>
  `,
  styles: [`
    .login-wrap {
      min-height: 100vh; display: flex; align-items: center; justify-content: center;
      background: linear-gradient(135deg, #0096a8 0%, #1a1a1a 100%);
    }
    .login-card { width: 380px; }
    h1 { color: #0096a8; font-size: 22px; margin: 0 0 4px; }
    .subtitle { color: #666; margin: 0 0 8px; font-size: 14px; }
    .hint { font-size: 11px; color: #888; margin-top: 20px; line-height: 1.6; }
  `]
})
export class LoginComponent {
  email = '';
  password = '';
  loading = false;
  error = '';

  constructor(private auth: AuthService, private router: Router) {}

  submit(): void {
    if (!this.email || !this.password) {
      this.error = 'Completati email-ul si parola.';
      return;
    }
    this.loading = true;
    this.error = '';
    this.auth.login(this.email, this.password).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/my-requests']);
      },
      error: (err) => {
        this.loading = false;
        this.error = err?.error?.message ?? 'Autentificare esuata.';
      }
    });
  }
}
