package com.stockflow.user;

import com.stockflow.role.Role;
import com.stockflow.role.RoleRepository;
import com.stockflow.exception.BadRequestException;
import com.stockflow.exception.ResourceNotFoundException;
import com.stockflow.security.RoleName;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public User findUserByEmail(String email) {

    return userRepository
            .findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getAllUsers() {
        return userRepository.findAllByOrderByIdAsc();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public User updateUserRole(Long id, UserRoleUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (isCurrentUser(user)) {
            throw new BadRequestException("You cannot change your own role");
        }

        String authority = parseRequestedAuthority(request.getRole());
        Role role = roleRepository.findByName(authority)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        user.setRole(role);
        return userRepository.save(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (isCurrentUser(user)) {
            throw new BadRequestException("You cannot delete your own account");
        }

        userRepository.delete(user);
    }

    private boolean isCurrentUser(User user) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof User currentUser)) {
            return false;
        }
        return currentUser.getId() != null && currentUser.getId().equals(user.getId());
    }

    private String parseRequestedAuthority(String rawRole) {
        if (rawRole == null || rawRole.isBlank()) {
            throw new BadRequestException("Role is required");
        }

        String normalized = rawRole.trim().toUpperCase();
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring(5);
        }

        for (RoleName roleName : RoleName.values()) {
            if (roleName.name().equals(normalized)) {
                return roleName.authority();
            }
        }

        throw new BadRequestException("Unsupported role: " + rawRole);
    }

}
