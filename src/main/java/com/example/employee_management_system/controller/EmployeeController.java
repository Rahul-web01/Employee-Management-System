package com.example.employee_management_system.controller;

import com.example.employee_management_system.entity.Department;
import com.example.employee_management_system.entity.Employee;
import com.example.employee_management_system.exception.DuplicateResourceException;
import com.example.employee_management_system.service.DepartmentService;
import com.example.employee_management_system.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final DepartmentService departmentService;

    public EmployeeController(EmployeeService employeeService,
                              DepartmentService departmentService) {
        this.employeeService = employeeService;
        this.departmentService = departmentService;
    }

    @GetMapping
    public String list(@RequestParam(name = "keyword", required = false) String keyword, Model model) {
        model.addAttribute("employees", employeeService.search(keyword));
        model.addAttribute("keyword", keyword);
        return "employees/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("employee", new Employee());
        model.addAttribute("departments", departmentService.findAll());
        model.addAttribute("selectedDepartmentId", null);
        return "employees/form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Employee employee = employeeService.findById(id);
        model.addAttribute("employee", employee);
        model.addAttribute("departments", departmentService.findAll());
        model.addAttribute("selectedDepartmentId",
                employee.getDepartment() != null ? employee.getDepartment().getId() : null);
        return "employees/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("employee") Employee employee,
                       BindingResult result,
                       @RequestParam(value = "departmentId", required = false) Long departmentId,
                       Model model) {

        // Department is chosen from a dropdown, so we check it by hand
        if (departmentId == null) {
            result.rejectValue("department", "department.required", "Please select a department");
        } else {
            Department department = departmentService.findById(departmentId);
            employee.setDepartment(department);
        }

        if (!result.hasErrors()) {
            try {
                if (employee.getId() == null) {
                    employeeService.create(employee);
                } else {
                    employeeService.update(employee.getId(), employee);
                }
                return "redirect:/employees";
            } catch (DuplicateResourceException ex) {
                result.rejectValue("email", "email.duplicate", ex.getMessage());
            }
        }

        // Something was wrong: show the form again with the messages
        model.addAttribute("departments", departmentService.findAll());
        model.addAttribute("selectedDepartmentId", departmentId);
        return "employees/form";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id) {
        employeeService.delete(id);
        return "redirect:/employees";
    }
}