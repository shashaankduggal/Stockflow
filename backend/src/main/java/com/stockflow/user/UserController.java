package com.stockflow.user;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/email/{email}")
    public User getUserByEmail(
            @PathVariable String email) {

        return userService.findUserByEmail(email);

    }

    

}