package com.booking.auth.config;

import com.booking.auth.model.Role;
import com.booking.auth.model.User;
import com.booking.auth.repository.RoleRepository;
import com.booking.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.init.admin.username:superadmin}")
    private String adminUsername;

    @Value("${app.init.admin.password:}")
    private String adminPassword;

    @Value("${app.init.admin.email:}")
    private String adminEmail;

    @Value("${app.init.legacy-admin.username:admin}")
    private String legacyAdminUsername;

    @Value("${app.init.legacy-admin.password:}")
    private String legacyAdminPassword;

    @Value("${app.init.legacy-admin.email:}")
    private String legacyAdminEmail;

    @Override
    public void run(String... args) {
        initializeRoles();
        initializeAdminUsers();
    }

    private void initializeRoles() {
        createRoleIfMissing(Role.SUPER_ADMIN, "Super Administrador del sistema");
        createRoleIfMissing(Role.BUSINESS_ADMIN, "Administrador de negocio");
        createRoleIfMissing(Role.ADMIN, "Administrador legacy");
        createRoleIfMissing(Role.USER, "Usuario regular");
    }

    private void createRoleIfMissing(String name, String description) {
        if (!roleRepository.existsByName(name)) {
            roleRepository.save(Role.builder()
                    .name(name)
                    .description(description)
                    .build());
            log.info("Rol {} creado en auth-service", name);
        }
    }

    private void initializeAdminUsers() {
        createAdminIfConfigured(adminUsername, adminEmail, adminPassword, Role.SUPER_ADMIN, "Super Administrador");
        createAdminIfConfigured(legacyAdminUsername, legacyAdminEmail, legacyAdminPassword, Role.ADMIN, "Administrador");
    }

    private void createAdminIfConfigured(
            String username,
            String email,
            String password,
            String roleName,
            String fullName) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(email) || !StringUtils.hasText(password)) {
            log.info("Seed de {} omitido: username, email o password no configurado", roleName);
            return;
        }

        if (userRepository.existsByUsername(username)) {
            return;
        }

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Rol " + roleName + " no encontrado"));

        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .fullName(fullName)
                .roles(Set.of(role))
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();

        userRepository.save(user);
        log.info("Usuario {} creado en auth-service con rol {}", username, roleName);
    }
}
