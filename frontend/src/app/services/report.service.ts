import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { DepartmentStats, LeaveStatus } from '../models/models';
import { LeaveRequestFilters } from './leave-request.service';

@Injectable({ providedIn: 'root' })
export class ReportService {
  private readonly base = `${environment.apiBaseUrl}/reports`;

  constructor(private http: HttpClient) {}

  leaveRequestPdf(id: number): Observable<Blob> {
    return this.http.get(`${this.base}/leave-requests/${id}/pdf`, { responseType: 'blob' });
  }

  requestsReportPdf(filters: LeaveRequestFilters): Observable<Blob> {
    const params: Record<string, string> = {};
    Object.entries(filters).forEach(([k, v]) => {
      if (v !== undefined && v !== null && v !== '') {
        params[k] = String(v);
      }
    });
    return this.http.get(`${this.base}/leave-requests/pdf`, { params, responseType: 'blob' });
  }

  balancesPdf(deptId?: number): Observable<Blob> {
    const params: Record<string, string> = deptId ? { deptId: String(deptId) } : {};
    return this.http.get(`${this.base}/balances/pdf`, { params, responseType: 'blob' });
  }

  departmentStats(deptId: number): Observable<DepartmentStats> {
    return this.http.get<DepartmentStats>(`${this.base}/departments/${deptId}/stats`);
  }

  static downloadBlob(blob: Blob, fileName: string): void {
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = fileName;
    a.click();
    window.URL.revokeObjectURL(url);
  }
}
