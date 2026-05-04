package com.booking.auth.config;

import com.booking.auth.model.Role;
import com.booking.auth.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
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
}
