package com.example.employee_management_system.service;

import com.example.employee_management_system.entity.Employee;
import com.example.employee_management_system.exception.DuplicateResourceException;
import com.example.employee_management_system.exception.ResourceNotFoundException;
import com.example.employee_management_system.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee employee(Long id, String email) {
        Employee e = new Employee();
        e.setId(id);
        e.setFirstName("Rahul");
        e.setLastName("Singh");
        e.setEmail(email);
        e.setDesignation("Developer");
        e.setSalary(new BigDecimal("50000.00"));
        return e;
    }

    @Test
    void findById_returnsEmployee_whenFound() {
        Employee e = employee(1L, "rahul@example.com");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(e));

        Employee result = employeeService.findById(1L);

        assertEquals("rahul@example.com", result.getEmail());
    }

    @Test
    void findById_throws_whenNotFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeService.findById(99L));
    }

    @Test
    void search_returnsAll_whenKeywordBlank() {
        List<Employee> all = List.of(employee(1L, "a@example.com"), employee(2L, "b@example.com"));
        when(employeeRepository.findAll()).thenReturn(all);

        List<Employee> result = employeeService.search("   ");

        assertEquals(2, result.size());
        verify(employeeRepository, never())
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(any(), any());
    }

    @Test
    void search_usesKeyword_whenProvided() {
        List<Employee> found = List.of(employee(1L, "a@example.com"));
        when(employeeRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("rah", "rah"))
                .thenReturn(found);

        List<Employee> result = employeeService.search("rah");

        assertEquals(1, result.size());
    }

    @Test
    void create_savesEmployee_whenEmailIsFree() {
        Employee e = employee(null, "new@example.com");
        when(employeeRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(employeeRepository.save(e)).thenReturn(e);

        Employee saved = employeeService.create(e);

        assertEquals("new@example.com", saved.getEmail());
        verify(employeeRepository).save(e);
    }

    @Test
    void create_throws_whenEmailAlreadyExists() {
        Employee e = employee(null, "taken@example.com");
        when(employeeRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> employeeService.create(e));

        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void update_changesFields_whenEmailUnchanged() {
        Employee existing = employee(1L, "rahul@example.com");
        Employee updated = employee(null, "rahul@example.com");
        updated.setDesignation("Senior Developer");
        updated.setSalary(new BigDecimal("90000.00"));

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

        Employee result = employeeService.update(1L, updated);

        assertEquals("Senior Developer", result.getDesignation());
        assertEquals(new BigDecimal("90000.00"), result.getSalary());
    }

    @Test
    void update_throws_whenNewEmailBelongsToSomeoneElse() {
        Employee existing = employee(1L, "old@example.com");
        Employee updated = employee(null, "new@example.com");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(employeeRepository.existsByEmail("new@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> employeeService.update(1L, updated));

        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void delete_removesEmployee_whenFound() {
        Employee e = employee(1L, "rahul@example.com");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(e));

        employeeService.delete(1L);

        verify(employeeRepository).delete(e);
    }

    @Test
    void delete_throws_whenNotFound() {
        when(employeeRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeService.delete(5L));

        verify(employeeRepository, never()).delete(any(Employee.class));
    }
}
