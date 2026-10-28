package com.example.employee_management_system.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(ResourceNotFoundException ex, Model model) {
        return errorPage(model, 404, "Not Found", ex.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNoResource(NoResourceFoundException ex, Model model) {
        return errorPage(model, 404, "Page Not Found", "The page you are looking for does not exist.");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadArgument(MethodArgumentTypeMismatchException ex, Model model) {
        return errorPage(model, 400, "Bad Request", "The request contains an invalid value.");
    }

    @ExceptionHandler(DuplicateResourceException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleDuplicate(DuplicateResourceException ex, Model model) {
        return errorPage(model, 409, "Duplicate Record", ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleIntegrity(DataIntegrityViolationException ex, Model model) {
        log.warn("Data integrity problem", ex);
        return errorPage(model, 409, "Cannot Complete Action",
                "This record is linked to other data or already exists.");
    }

    // Safety net: any other unexpected error
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleAny(Exception ex, Model model) {
        log.error("Unexpected error", ex);
        return errorPage(model, 500, "Something Went Wrong",
                "An unexpected error occurred. Please try again later.");
    }

    private String errorPage(Model model, int status, String title, String message) {
        model.addAttribute("status", status);
        model.addAttribute("title", title);
        model.addAttribute("message", message);
        return "error/error";
    }
}