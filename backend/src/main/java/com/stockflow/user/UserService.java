package com.stockflow.user;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public User findUserByEmail(String email) {

    return userRepository
            .findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

    }

}
