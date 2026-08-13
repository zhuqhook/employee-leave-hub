package com.draxlmaier.leavehub.controller;

import com.draxlmaier.leavehub.dto.EmployeeDto;
import com.draxlmaier.leavehub.dto.LoginRequest;
import com.draxlmaier.leavehub.dto.LoginResponse;
import com.draxlmaier.leavehub.entity.Employee;
import com.draxlmaier.leavehub.repository.EmployeeRepository;
import com.draxlmaier.leavehub.security.EmployeePrincipal;
import com.draxlmaier.leavehub.security.JwtUtil;
import com.draxlmaier.leavehub.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final EmployeeRepository employeeRepository;
    private final EmployeeService employeeService;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (org.springframework.security.core.AuthenticationException ex) {
            throw new BadCredentialsException("Email sau parola incorecte.");
        }

        Employee employee = employeeRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Email sau parola incorecte."));

        String token = jwtUtil.generateToken(employee.getEmail(), employee.getEmplId(), employee.getRole().name());

        return LoginResponse.builder()
                .token(token)
                .employee(employeeService.toDto(employee))
                .build();
    }

    @GetMapping("/me")
    public EmployeeDto me(@org.springframework.security.core.annotation.AuthenticationPrincipal EmployeePrincipal principal) {
        return employeeService.toDto(principal.getEmployee());
    }
}
