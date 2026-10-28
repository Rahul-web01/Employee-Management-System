package com.example.employee_management_system.service;

import com.example.employee_management_system.entity.Department;
import com.example.employee_management_system.exception.ResourceNotFoundException;
import com.example.employee_management_system.repository.DepartmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private DepartmentService departmentService;

    @Test
    void findById_returnsDepartment_whenFound() {
        Department d = new Department();
        d.setName("Engineering");
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(d));

        assertEquals("Engineering", departmentService.findById(1L).getName());
    }

    @Test
    void findById_throws_whenNotFound() {
        when(departmentRepository.findById(7L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> departmentService.findById(7L));
    }

    @Test
    void save_returnsSavedDepartment() {
        Department d = new Department();
        d.setName("Finance");
        when(departmentRepository.save(d)).thenReturn(d);

        assertEquals("Finance", departmentService.save(d).getName());
    }
}
