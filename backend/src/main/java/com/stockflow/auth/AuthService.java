package com.stockflow.auth;

import com.stockflow.role.Role;
import com.stockflow.role.RoleRepository;
import com.stockflow.exception.BadRequestException;
import com.stockflow.exception.UnauthorizedException;
import com.stockflow.security.RoleName;
import com.stockflow.user.User;
import com.stockflow.user.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {

        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new UnauthorizedException("Invalid email or password");
        }

        String authority = RoleName.normalizeAuthority(user.getRole() != null ? user.getRole().getName() : null);
        String token = jwtService.generateToken(user.getEmail(), authority);

        return new LoginResponse(
                "Login successful",
                token,
                user.getFullName(),
                user.getEmail(),
                RoleName.normalizeLabel(authority));
    }

    @Transactional
    public LoginResponse signup(SignupRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.findByEmail(email).isPresent()) {
            throw new BadRequestException("An account with that email already exists");
        }

        Role role = roleRepository.findByName(RoleName.VIEWER.authority())
                .orElseGet(() -> roleRepository.save(new Role(null, RoleName.VIEWER.authority())));

        User user = new User();
        user.setFullName(request.getFullName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(true);
        user.setRole(role);

        user = userRepository.save(user);

        String authority = RoleName.normalizeAuthority(role.getName());
        String token = jwtService.generateToken(user.getEmail(), authority);

        return new LoginResponse(
                "Signup successful",
                token,
                user.getFullName(),
                user.getEmail(),
                RoleName.normalizeLabel(authority));
    }
}
