package com.example.employee_management_system.security;

import com.example.employee_management_system.entity.Department;
import com.example.employee_management_system.entity.Employee;
import com.example.employee_management_system.repository.DepartmentRepository;
import com.example.employee_management_system.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Checks who can open which page: signed-out visitors, VIEWER, HR and ADMIN.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SecurityAccessTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    private Department department;

    @BeforeEach
    void setUp() {
        Department d = new Department();
        d.setName("Security Test Dept");
        d.setLocation("Lucknow");
        department = departmentRepository.save(d);
    }

    private Employee saveEmployee(String email) {
        Employee e = new Employee();
        e.setFirstName("Test");
        e.setLastName("Person");
        e.setEmail(email);
        e.setDesignation("Tester");
        e.setSalary(new BigDecimal("50000"));
        e.setHireDate(LocalDate.of(2024, 1, 1));
        e.setDepartment(department);
        return employeeRepository.save(e);
    }

    // ---------- signing in and out ----------

    @Test
    void signedOutVisitorIsSentToLoginPage() throws Exception {
        mvc.perform(get("/employees"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void loginPageIsPublic() throws Exception {
        mvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Sign in")));
    }

    @Test
    void seededAdminCanSignInWithConfiguredPassword() throws Exception {
        mvc.perform(formLogin().user("admin").password("admin123"))
                .andExpect(authenticated().withUsername("admin").withRoles("ADMIN"))
                .andExpect(redirectedUrl("/employees"));
    }

    @Test
    void seededViewerCanSignIn() throws Exception {
        mvc.perform(formLogin().user("viewer").password("viewer123"))
                .andExpect(authenticated().withUsername("viewer").withRoles("VIEWER"));
    }

    @Test
    void wrongPasswordIsRejected() throws Exception {
        mvc.perform(formLogin().user("admin").password("wrong-password"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void logoutSignsTheUserOut() throws Exception {
        mvc.perform(post("/logout").with(csrf()).with(user("viewer").roles("VIEWER")))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?logout"));
    }

    // ---------- VIEWER: read-only ----------

    @Test
    @WithMockUser(username = "viewer", roles = "VIEWER")
    void viewerSeesListWithoutAddEditOrDeleteButtons() throws Exception {
        saveEmployee("viewer.list@example.com");

        mvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("viewer.list@example.com")))
                .andExpect(content().string(not(containsString("+ Add Employee"))))
                .andExpect(content().string(not(containsString(">Edit<"))))
                .andExpect(content().string(not(containsString(">Delete<"))));
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void viewerCannotOpenTheAddForm() throws Exception {
        mvc.perform(get("/employees/new"))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/access-denied"));
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void viewerCannotSaveAnEmployee() throws Exception {
        mvc.perform(post("/employees/save").with(csrf())
                        .param("firstName", "Blocked")
                        .param("lastName", "Viewer")
                        .param("email", "blocked.viewer@example.com")
                        .param("salary", "1000")
                        .param("departmentId", department.getId().toString()))
                .andExpect(status().isForbidden());

        assertFalse(employeeRepository.existsByEmail("blocked.viewer@example.com"));
    }

    // ---------- HR: add and edit, no delete ----------

    @Test
    @WithMockUser(roles = "HR")
    void hrCanAddAnEmployee() throws Exception {
        mvc.perform(post("/employees/save").with(csrf())
                        .param("firstName", "Hired")
                        .param("lastName", "ByHr")
                        .param("email", "hired.byhr@example.com")
                        .param("designation", "Analyst")
                        .param("salary", "45000")
                        .param("hireDate", "2025-01-15")
                        .param("departmentId", department.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"));

        assertTrue(employeeRepository.existsByEmail("hired.byhr@example.com"));
    }

    @Test
    @WithMockUser(roles = "HR")
    void hrCanOpenTheEditFormButSeesNoDeleteButton() throws Exception {
        Employee e = saveEmployee("hr.edit@example.com");

        mvc.perform(get("/employees/edit/{id}", e.getId()))
                .andExpect(status().isOk());

        mvc.perform(get("/employees"))
                .andExpect(content().string(containsString(">Edit<")))
                .andExpect(content().string(not(containsString(">Delete<"))));
    }

    @Test
    @WithMockUser(roles = "HR")
    void hrCannotDeleteAnEmployee() throws Exception {
        Employee e = saveEmployee("hr.nodelete@example.com");

        mvc.perform(post("/employees/delete/{id}", e.getId()).with(csrf()))
                .andExpect(status().isForbidden());

        assertTrue(employeeRepository.existsById(e.getId()));
    }

    // ---------- ADMIN: everything ----------

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanDeleteAnEmployee() throws Exception {
        Employee e = saveEmployee("admin.delete@example.com");

        mvc.perform(post("/employees/delete/{id}", e.getId()).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"));

        assertFalse(employeeRepository.existsById(e.getId()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminSeesDeleteButtons() throws Exception {
        saveEmployee("admin.list@example.com");

        mvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(">Delete<")))
                .andExpect(content().string(containsString("+ Add Employee")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void formPostWithoutCsrfTokenIsRejected() throws Exception {
        Employee e = saveEmployee("csrf.check@example.com");

        mvc.perform(post("/employees/delete/{id}", e.getId()))
                .andExpect(status().isForbidden());

        assertTrue(employeeRepository.existsById(e.getId()));
    }
}
