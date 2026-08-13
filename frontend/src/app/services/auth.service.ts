import { Injectable, computed, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { Employee, LoginResponse, Role } from '../models/models';

const TOKEN_KEY = 'elh_token';
const EMPLOYEE_KEY = 'elh_employee';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly currentEmployeeSignal = signal<Employee | null>(this.readStoredEmployee());
  readonly currentEmployee = this.currentEmployeeSignal.asReadonly();
  readonly isLoggedIn = computed(() => this.currentEmployeeSignal() !== null);
  readonly role = computed<Role | null>(() => this.currentEmployeeSignal()?.role ?? null);
  readonly isManagerOrAdmin = computed(() => {
    const r = this.role();
    return r === 'ADMIN' || r === 'DEPARTMENT_MANAGER';
  });
  readonly isAdmin = computed(() => this.role() === 'ADMIN');

  constructor(private http: HttpClient) {}

  login(email: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${environment.apiBaseUrl}/auth/login`, { email, password }).pipe(
      tap((res) => {
        localStorage.setItem(TOKEN_KEY, res.token);
        localStorage.setItem(EMPLOYEE_KEY, JSON.stringify(res.employee));
        this.currentEmployeeSignal.set(res.employee);
      })
    );
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(EMPLOYEE_KEY);
    this.currentEmployeeSignal.set(null);
  }

  refreshMe(): Observable<Employee> {
    return this.http.get<Employee>(`${environment.apiBaseUrl}/auth/me`).pipe(
      tap((employee) => {
        localStorage.setItem(EMPLOYEE_KEY, JSON.stringify(employee));
        this.currentEmployeeSignal.set(employee);
      })
    );
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  private readStoredEmployee(): Employee | null {
    const raw = localStorage.getItem(EMPLOYEE_KEY);
    return raw ? (JSON.parse(raw) as Employee) : null;
  }
}
