package com.stockflow.role;

import com.stockflow.security.RoleName;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class RoleBootstrap implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public RoleBootstrap(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) {
        for (RoleName roleName : RoleName.values()) {
            roleRepository.findByName(roleName.authority())
                    .orElseGet(() -> roleRepository.save(new Role(null, roleName.authority())));
        }
    }
}
