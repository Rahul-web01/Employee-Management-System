package com.example.employee_management_system.service;

import com.example.employee_management_system.entity.Employee;
import com.example.employee_management_system.exception.DuplicateResourceException;
import com.example.employee_management_system.exception.ResourceNotFoundException;
import com.example.employee_management_system.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    public Employee findById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id " + id));
    }

    public List<Employee> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }
        return employeeRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(keyword, keyword);
    }

    public Employee create(Employee employee) {
        if (employeeRepository.existsByEmail(employee.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + employee.getEmail());
        }
        return employeeRepository.save(employee);
    }

    public Employee update(Long id, Employee updated) {
        Employee existing = findById(id);

        if (!existing.getEmail().equals(updated.getEmail())
                && employeeRepository.existsByEmail(updated.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + updated.getEmail());
        }

        existing.setFirstName(updated.getFirstName());
        existing.setLastName(updated.getLastName());
        existing.setEmail(updated.getEmail());
        existing.setDesignation(updated.getDesignation());
        existing.setSalary(updated.getSalary());
        existing.setHireDate(updated.getHireDate());
        existing.setDepartment(updated.getDepartment());

        return employeeRepository.save(existing);
    }

    public void delete(Long id) {
        Employee employee = findById(id);
        employeeRepository.delete(employee);
    }
}