package com.example.employee_management_system.entity;

/**
 * What a signed-in user is allowed to do.
 * ADMIN: everything. HR: add and edit employees. VIEWER: read-only.
 */
public enum Role {
    ADMIN,
    HR,
    VIEWER
}
