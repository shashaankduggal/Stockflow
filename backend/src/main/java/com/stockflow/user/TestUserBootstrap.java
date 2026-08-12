package com.stockflow.user;

import com.stockflow.role.Role;
import com.stockflow.role.RoleRepository;
import com.stockflow.security.RoleName;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class TestUserBootstrap implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public TestUserBootstrap(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        ensureAdminUser("System Administrator", "admin@stockflow.com", "Admin@123", RoleName.ADMIN);
        seedUser("Operations Manager", "manager@stockflow.com", "Manager@123", RoleName.MANAGER);
        seedUser("Warehouse Staff", "staff@stockflow.com", "Staff@123", RoleName.STAFF);
        seedUser("External Viewer", "viewer@stockflow.com", "Viewer@123", RoleName.VIEWER);
    }

    private void ensureAdminUser(String fullName, String email, String rawPassword, RoleName roleName) {
        Role role = roleRepository.findByName(roleName.authority())
                .orElseThrow(() -> new IllegalStateException("Role not found: " + roleName.authority()));

        User user = userRepository.findByEmail(email).orElseGet(User::new);
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setEnabled(true);
        user.setRole(role);

        userRepository.save(user);
    }

    private void seedUser(String fullName, String email, String rawPassword, RoleName roleName) {
        if (userRepository.findByEmail(email).isPresent()) {
            return;
        }

        Role role = roleRepository.findByName(roleName.authority())
                .orElseThrow(() -> new IllegalStateException("Role not found: " + roleName.authority()));

        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setEnabled(true);
        user.setRole(role);

        userRepository.save(user);
    }
}
