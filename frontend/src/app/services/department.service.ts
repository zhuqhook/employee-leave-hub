import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Department } from '../models/models';

@Injectable({ providedIn: 'root' })
export class DepartmentService {
  private readonly base = `${environment.apiBaseUrl}/departments`;

  constructor(private http: HttpClient) {}

  findAll(): Observable<Department[]> {
    return this.http.get<Department[]>(this.base);
  }

  create(dto: Partial<Department>): Observable<Department> {
    return this.http.post<Department>(this.base, dto);
  }

  update(id: number, dto: Partial<Department>): Observable<Department> {
    return this.http.put<Department>(`${this.base}/${id}`, dto);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
