import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { LeaveType } from '../models/models';

@Injectable({ providedIn: 'root' })
export class LeaveTypeService {
  private readonly base = `${environment.apiBaseUrl}/leave-types`;

  constructor(private http: HttpClient) {}

  findAll(): Observable<LeaveType[]> {
    return this.http.get<LeaveType[]>(this.base);
  }

  create(dto: Partial<LeaveType>): Observable<LeaveType> {
    return this.http.post<LeaveType>(this.base, dto);
  }

  update(id: number, dto: Partial<LeaveType>): Observable<LeaveType> {
    return this.http.put<LeaveType>(`${this.base}/${id}`, dto);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
