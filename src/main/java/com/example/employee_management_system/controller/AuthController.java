package com.example.employee_management_system.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class AuthController {

    private final boolean showDemoLogins;

    public AuthController(@Value("${app.demo.show-logins:true}") boolean showDemoLogins) {
        this.showDemoLogins = showDemoLogins;
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("showDemoLogins", showDemoLogins);
        return "login";
    }

    // No HTTP method restriction: Spring Security forwards here from GET and POST requests alike
    @RequestMapping("/access-denied")
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String accessDenied(Model model) {
        model.addAttribute("status", 403);
        model.addAttribute("title", "Access Denied");
        model.addAttribute("message", "Your account does not have permission to do that.");
        return "error/error";
    }
}
