package com.example.employee_management_system.entity;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private Employee validEmployee() {
        Employee e = new Employee();
        e.setFirstName("Rahul");
        e.setLastName("Singh");
        e.setEmail("rahul@example.com");
        e.setSalary(new BigDecimal("50000.00"));
        e.setHireDate(LocalDate.now());
        return e;
    }

    private Set<String> invalidFields(Employee e) {
        return validator.validate(e).stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    @Test
    void validEmployee_hasNoViolations() {
        assertTrue(invalidFields(validEmployee()).isEmpty());
    }

    @Test
    void emptyEmployee_failsRequiredFields() {
        Set<String> fields = invalidFields(new Employee());

        assertTrue(fields.contains("firstName"));
        assertTrue(fields.contains("lastName"));
        assertTrue(fields.contains("email"));
        assertTrue(fields.contains("salary"));
    }

    @Test
    void invalidEmail_isRejected() {
        Employee e = validEmployee();
        e.setEmail("abc");

        assertTrue(invalidFields(e).contains("email"));
    }

    @Test
    void shortFirstName_isRejected() {
        Employee e = validEmployee();
        e.setFirstName("R");

        assertTrue(invalidFields(e).contains("firstName"));
    }

    @Test
    void negativeSalary_isRejected() {
        Employee e = validEmployee();
        e.setSalary(new BigDecimal("-1"));

        assertTrue(invalidFields(e).contains("salary"));
    }

    @Test
    void futureHireDate_isRejected() {
        Employee e = validEmployee();
        e.setHireDate(LocalDate.now().plusDays(5));

        assertTrue(invalidFields(e).contains("hireDate"));
    }
}
