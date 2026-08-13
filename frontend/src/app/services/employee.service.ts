import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Employee, EmployeeCreate } from '../models/models';

@Injectable({ providedIn: 'root' })
export class EmployeeService {
  private readonly base = `${environment.apiBaseUrl}/employees`;

  constructor(private http: HttpClient) {}

  findAll(): Observable<Employee[]> {
    return this.http.get<Employee[]>(this.base);
  }

  create(dto: EmployeeCreate): Observable<Employee> {
    return this.http.post<Employee>(this.base, dto);
  }

  update(id: number, dto: EmployeeCreate): Observable<Employee> {
    return this.http.put<Employee>(`${this.base}/${id}`, dto);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
