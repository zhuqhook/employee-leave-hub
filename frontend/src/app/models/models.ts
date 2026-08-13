export type Role = 'USER' | 'DEPARTMENT_MANAGER' | 'ADMIN';

export type LeaveStatus = 'DRAFT' | 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED';

export interface Employee {
  emplId: number;
  name: string;
  email: string;
  role: Role;
  deptId: number | null;
  departmentName: string | null;
  annualLeaveDays: number;
  availableLeaveDays: number;
  active: boolean;
}

export interface EmployeeCreate {
  name: string;
  email: string;
  password?: string;
  role: Role;
  deptId: number | null;
  annualLeaveDays: number;
  active?: boolean;
}

export interface Department {
  deptId: number;
  departmentName: string;
  managerId: number | null;
  managerName: string | null;
  maxAbsentEmployees: number;
}

export interface LeaveType {
  leaveTypeId: number;
  name: string;
  code: string;
  requiresAttachment: boolean;
  paid: boolean;
}

export interface Attachment {
  attachmentId: number;
  fileName: string;
  uploadedAt: string;
}

export interface WorkflowHistory {
  oldStatus: LeaveStatus | null;
  currentStatus: LeaveStatus;
  changedByName: string;
  changedAt: string;
  comment: string | null;
}

export interface LeaveRequest {
  leaveRequestId: number;
  emplId: number;
  employeeName: string;
  deptId: number | null;
  departmentName: string | null;
  leaveTypeId: number;
  leaveTypeName: string;
  leaveTypeCode: string;
  startDate: string;
  endDate: string;
  workingDays: number;
  status: LeaveStatus;
  createdAt: string;
  attachments: Attachment[];
  history: WorkflowHistory[];
}

export interface LeaveRequestCreate {
  leaveTypeId: number;
  startDate: string;
  endDate: string;
  submit: boolean;
}

export interface CalendarEntry {
  leaveRequestId: number;
  emplId: number;
  employeeName: string;
  leaveTypeCode: string;
  startDate: string;
  endDate: string;
}

export interface DepartmentStats {
  deptId: number;
  departmentName: string;
  totalRequests: number;
  pendingRequests: number;
  approvedRequests: number;
  rejectedRequests: number;
  totalDaysConsumed: number;
  employeeCount: number;
}

export interface LoginResponse {
  token: string;
  employee: Employee;
}
