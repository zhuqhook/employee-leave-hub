package com.draxlmaier.leavehub.config;

import com.draxlmaier.leavehub.entity.*;
import com.draxlmaier.leavehub.repository.DepartmentRepository;
import com.draxlmaier.leavehub.repository.EmployeeRepository;
import com.draxlmaier.leavehub.repository.LeaveTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Populeaza baza de date cu date initiale (departamente, tipuri de concediu, cont admin)
 * daca aceasta este goala. Util pentru prima rulare / demo.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final DepartmentRepository departmentRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedLeaveTypes();
        seedDepartmentsAndAdmin();
    }

    private void seedLeaveTypes() {
        if (leaveTypeRepository.count() > 0) {
            return;
        }
        leaveTypeRepository.save(LeaveType.builder()
                .name("Concediu de odihna").code("CO").requiresAttachment(false).paid(true).build());
        leaveTypeRepository.save(LeaveType.builder()
                .name("Concediu medical").code("CM").requiresAttachment(true).paid(true).build());
        leaveTypeRepository.save(LeaveType.builder()
                .name("Concediu fara plata").code("FP").requiresAttachment(false).paid(false).build());
        leaveTypeRepository.save(LeaveType.builder()
                .name("Eveniment special").code("SPECIAL").requiresAttachment(false).paid(true).build());
    }

    private void seedDepartmentsAndAdmin() {
        if (employeeRepository.count() > 0) {
            return;
        }

        Department it = departmentRepository.save(Department.builder()
                .departmentName("IT")
                .maxAbsentEmployees(3)
                .build());

        departmentRepository.save(Department.builder()
                .departmentName("HR")
                .maxAbsentEmployees(2)
                .build());

        departmentRepository.save(Department.builder()
                .departmentName("Productie")
                .maxAbsentEmployees(5)
                .build());

        Employee admin = Employee.builder()
                .name("Administrator")
                .email("admin@draxlmaier.com")
                .passwordHash(passwordEncoder.encode("Admin123!"))
                .role(Role.ADMIN)
                .department(it)
                .annualLeaveDays(21)
                .availableLeaveDays(21)
                .active(true)
                .build();
        employeeRepository.save(admin);

        Employee manager = Employee.builder()
                .name("Ion Popescu")
                .email("manager.it@draxlmaier.com")
                .passwordHash(passwordEncoder.encode("Manager123!"))
                .role(Role.DEPARTMENT_MANAGER)
                .department(it)
                .annualLeaveDays(21)
                .availableLeaveDays(21)
                .active(true)
                .build();
        manager = employeeRepository.save(manager);

        it.setManager(manager);
        departmentRepository.save(it);

        Employee user = Employee.builder()
                .name("Maria Ionescu")
                .email("maria.ionescu@draxlmaier.com")
                .passwordHash(passwordEncoder.encode("User123!"))
                .role(Role.USER)
                .department(it)
                .annualLeaveDays(21)
                .availableLeaveDays(21)
                .active(true)
                .build();
        employeeRepository.save(user);
    }
}
