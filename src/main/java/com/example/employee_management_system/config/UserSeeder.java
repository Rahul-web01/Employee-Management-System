package com.example.employee_management_system.config;

import com.example.employee_management_system.entity.AppUser;
import com.example.employee_management_system.entity.Role;
import com.example.employee_management_system.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Makes sure the three sign-in accounts (admin, hr, viewer) exist on every start.
 *
 * Passwords come from configuration (ADMIN_PASSWORD, HR_PASSWORD, VIEWER_PASSWORD
 * environment variables). If a password is changed there, the stored hash is
 * updated on the next start, so the environment variable is always the source of truth.
 */
@Component
public class UserSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(UserSeeder.class);

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminPassword;
    private final String hrPassword;
    private final String viewerPassword;

    public UserSeeder(AppUserRepository userRepository,
                      PasswordEncoder passwordEncoder,
                      @Value("${app.users.admin.password:admin123}") String adminPassword,
                      @Value("${app.users.hr.password:hr123}") String hrPassword,
                      @Value("${app.users.viewer.password:viewer123}") String viewerPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminPassword = adminPassword;
        this.hrPassword = hrPassword;
        this.viewerPassword = viewerPassword;
    }

    @Override
    public void run(String... args) {
        ensureUser("admin", adminPassword, Role.ADMIN);
        ensureUser("hr", hrPassword, Role.HR);
        ensureUser("viewer", viewerPassword, Role.VIEWER);
    }

    private void ensureUser(String username, String rawPassword, Role role) {
        AppUser user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            userRepository.save(new AppUser(username, passwordEncoder.encode(rawPassword), role));
            log.info("Created user '{}' with role {}", username, role);
            return;
        }

        boolean changed = false;
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            user.setPassword(passwordEncoder.encode(rawPassword));
            changed = true;
        }
        if (user.getRole() != role) {
            user.setRole(role);
            changed = true;
        }
        if (changed) {
            userRepository.save(user);
            log.info("Updated user '{}' from configuration", username);
        }
    }
}
