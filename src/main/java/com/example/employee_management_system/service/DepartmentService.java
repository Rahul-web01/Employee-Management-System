package com.example.employee_management_system.service;

import com.example.employee_management_system.entity.Department;
import com.example.employee_management_system.exception.ResourceNotFoundException;
import com.example.employee_management_system.repository.DepartmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    public List<Department> findAll() {
        return departmentRepository.findAll();
    }

    public Department findById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id " + id));
    }

    public Department save(Department department) {
        return departmentRepository.save(department);
    }

    public void delete(Long id) {
        Department department = findById(id);
        departmentRepository.delete(department);
    }
}