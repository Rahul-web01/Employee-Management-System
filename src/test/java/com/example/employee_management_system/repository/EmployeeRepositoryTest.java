package com.example.employee_management_system.repository;

import com.example.employee_management_system.entity.Department;
import com.example.employee_management_system.entity.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    private Department department;

    @BeforeEach
    void setUp() {
        Department d = new Department();
        d.setName("Test Department");
        d.setLocation("Lucknow");
        department = departmentRepository.save(d);
    }

    private Employee saveEmployee(String first, String last, String email) {
        Employee e = new Employee();
        e.setFirstName(first);
        e.setLastName(last);
        e.setEmail(email);
        e.setSalary(new BigDecimal("40000.00"));
        e.setDepartment(department);
        return employeeRepository.saveAndFlush(e);
    }

    @Test
    void findByEmail_returnsEmployee_whenExists() {
        saveEmployee("Rahul", "Singh", "rahul@example.com");

        assertTrue(employeeRepository.findByEmail("rahul@example.com").isPresent());
    }

    @Test
    void findByEmail_returnsEmpty_whenMissing() {
        assertTrue(employeeRepository.findByEmail("nobody@example.com").isEmpty());
    }

    @Test
    void existsByEmail_isTrueOnlyForSavedEmail() {
        saveEmployee("Rahul", "Singh", "rahul@example.com");

        assertTrue(employeeRepository.existsByEmail("rahul@example.com"));
        assertFalse(employeeRepository.existsByEmail("other@example.com"));
    }

    @Test
    void findByDepartmentId_returnsOnlyThatDepartmentsEmployees() {
        saveEmployee("Asha", "Verma", "asha@example.com");
        saveEmployee("Ravi", "Kumar", "ravi@example.com");

        List<Employee> result = employeeRepository.findByDepartmentId(department.getId());

        assertEquals(2, result.size());
    }

    @Test
    void search_matchesFirstName_ignoringCase() {
        saveEmployee("Rahul", "Singh", "rahul@example.com");
        saveEmployee("Asha", "Verma", "asha@example.com");

        List<Employee> result = employeeRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("RAH", "RAH");

        assertEquals(1, result.size());
        assertEquals("Rahul", result.get(0).getFirstName());
    }

    @Test
    void search_matchesLastName() {
        saveEmployee("Rahul", "Singh", "rahul@example.com");
        saveEmployee("Asha", "Verma", "asha@example.com");

        List<Employee> result = employeeRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("verma", "verma");

        assertEquals(1, result.size());
        assertEquals("Asha", result.get(0).getFirstName());
    }

    @Test
    void saving_duplicateEmail_isRejectedByDatabase() {
        saveEmployee("Rahul", "Singh", "same@example.com");

        assertThrows(DataIntegrityViolationException.class,
                () -> saveEmployee("Another", "Person", "same@example.com"));
    }
}
