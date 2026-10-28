package com.example.employee_management_system.config;

import com.example.employee_management_system.entity.Department;
import com.example.employee_management_system.repository.DepartmentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final DepartmentRepository departmentRepository;

    public DataLoader(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Override
    public void run(String... args) {
        if (departmentRepository.count() == 0) {
            departmentRepository.save(create("Engineering", "Lucknow"));
            departmentRepository.save(create("Human Resources", "Delhi"));
            departmentRepository.save(create("Finance", "Mumbai"));
            departmentRepository.save(create("Marketing", "Bengaluru"));
        }
    }

    private Department create(String name, String location) {
        Department d = new Department();
        d.setName(name);
        d.setLocation(location);
        return d;
    }
}