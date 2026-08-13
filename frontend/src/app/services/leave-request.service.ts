import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Attachment, CalendarEntry, LeaveRequest, LeaveRequestCreate, LeaveStatus } from '../models/models';

export interface LeaveRequestFilters {
  deptId?: number;
  status?: LeaveStatus;
  leaveTypeId?: number;
  emplId?: number;
  from?: string;
  to?: string;
}

@Injectable({ providedIn: 'root' })
export class LeaveRequestService {
  private readonly base = `${environment.apiBaseUrl}/leave-requests`;

  constructor(private http: HttpClient) {}

  create(dto: LeaveRequestCreate): Observable<LeaveRequest> {
    return this.http.post<LeaveRequest>(this.base, dto);
  }

  addAttachment(id: number, file: File): Observable<Attachment> {
    const form = new FormData();
    form.append('file', file);
    return this.http.post<Attachment>(`${this.base}/${id}/attachments`, form);
  }

  submit(id: number): Observable<LeaveRequest> {
    return this.http.post<LeaveRequest>(`${this.base}/${id}/submit`, {});
  }

  cancel(id: number): Observable<LeaveRequest> {
    return this.http.post<LeaveRequest>(`${this.base}/${id}/cancel`, {});
  }

  mine(): Observable<LeaveRequest[]> {
    return this.http.get<LeaveRequest[]>(`${this.base}/mine`);
  }

  findById(id: number): Observable<LeaveRequest> {
    return this.http.get<LeaveRequest>(`${this.base}/${id}`);
  }

  approve(id: number, comment: string | null, override = false): Observable<LeaveRequest> {
    return this.http.post<LeaveRequest>(`${this.base}/${id}/approve?override=${override}`, { comment });
  }

  reject(id: number, comment: string): Observable<LeaveRequest> {
    return this.http.post<LeaveRequest>(`${this.base}/${id}/reject`, { comment });
  }

  search(filters: LeaveRequestFilters): Observable<LeaveRequest[]> {
    let params: Record<string, string> = {};
    Object.entries(filters).forEach(([k, v]) => {
      if (v !== undefined && v !== null && v !== '') {
        params[k] = String(v);
      }
    });
    return this.http.get<LeaveRequest[]>(this.base, { params });
  }

  calendar(deptId: number, from: string, to: string): Observable<CalendarEntry[]> {
    return this.http.get<CalendarEntry[]>(`${this.base}/calendar`, { params: { deptId, from, to } });
  }

  downloadAttachment(requestId: number, attachmentId: number): Observable<Blob> {
    return this.http.get(`${this.base}/${requestId}/attachments/${attachmentId}`, { responseType: 'blob' });
  }
}
