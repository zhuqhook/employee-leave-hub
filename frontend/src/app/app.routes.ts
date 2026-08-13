import { Routes } from '@angular/router';
import { authGuard, managerGuard, adminGuard } from './core/auth.guard';

export const routes: Routes = [
  { path: 'login', loadComponent: () => import('./pages/login/login.component').then(m => m.LoginComponent) },

  {
    path: 'my-requests',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/my-requests/my-requests.component').then(m => m.MyRequestsComponent)
  },
  {
    path: 'my-requests/new',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/new-request/new-request.component').then(m => m.NewRequestComponent)
  },
  {
    path: 'requests/:id',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/request-detail/request-detail.component').then(m => m.RequestDetailComponent)
  },
  {
    path: 'approvals',
    canActivate: [managerGuard],
    loadComponent: () => import('./pages/approvals/approvals.component').then(m => m.ApprovalsComponent)
  },
  {
    path: 'calendar',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/calendar/calendar.component').then(m => m.CalendarComponent)
  },
  {
    path: 'admin/employees',
    canActivate: [adminGuard],
    loadComponent: () => import('./pages/admin-employees/admin-employees.component').then(m => m.AdminEmployeesComponent)
  },
  {
    path: 'admin/departments',
    canActivate: [adminGuard],
    loadComponent: () => import('./pages/admin-departments/admin-departments.component').then(m => m.AdminDepartmentsComponent)
  },
  {
    path: 'admin/leave-types',
    canActivate: [adminGuard],
    loadComponent: () => import('./pages/admin-leave-types/admin-leave-types.component').then(m => m.AdminLeaveTypesComponent)
  },

  { path: '', pathMatch: 'full', redirectTo: 'my-requests' },
  { path: '**', redirectTo: 'my-requests' }
];
