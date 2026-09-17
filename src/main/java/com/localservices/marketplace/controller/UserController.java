package com.localservices.marketplace.controller;

import com.localservices.marketplace.model.User;
import com.localservices.marketplace.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {

        if (userService.emailExists(user.getEmail())) {
            return ResponseEntity.badRequest()
                    .body("Email already registered");
        }

        User savedUser = userService.registerUser(user);

        savedUser.setPassword(null);

        return ResponseEntity.ok(savedUser);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {

        return userService.findByEmail(user.getEmail())
                .map(existingUser -> {

                    if (existingUser.getPassword()
                            .equals(user.getPassword())) {

                        existingUser.setPassword(null);
                        return ResponseEntity.ok(existingUser);
                    }

                    return ResponseEntity.badRequest()
                            .body("Invalid password");
                })
                .orElse(
                    ResponseEntity.badRequest()
                            .body("User not found")
                );
    }
}