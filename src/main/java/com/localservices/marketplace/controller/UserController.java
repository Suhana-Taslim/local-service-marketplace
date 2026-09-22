package com.localservices.marketplace.controller;

import com.localservices.marketplace.model.ServiceProvider;
import com.localservices.marketplace.model.User;
import com.localservices.marketplace.service.ServiceProviderService;
import com.localservices.marketplace.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {

    private final UserService userService;
    private final ServiceProviderService serviceProviderService;

    public UserController(
            UserService userService,
            ServiceProviderService serviceProviderService) {

        this.userService = userService;
        this.serviceProviderService = serviceProviderService;
    }

    // Customer registration
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {

        String username = userService.normalizeUsername(user.getUsername());

        if (username == null || username.isBlank()) {
            return ResponseEntity.badRequest()
                .body("Username is required");
        }

        if (!username.matches("[a-z0-9][a-z0-9._-]{2,29}")) {
            return ResponseEntity.badRequest()
                .body("Username must be 3-30 characters using letters, numbers, '.', '_' or '-'");
        }

        user.setUsername(username);

        if (userService.usernameExists(username)) {
            return ResponseEntity.badRequest()
                .body("Username already taken");
        }

        user.setRole("CUSTOMER");

        User savedUser = userService.registerUser(user);

        Map<String, Object> response = new HashMap<>();
        response.put("id", savedUser.getId());
        response.put("name", savedUser.getName());
        response.put("username", savedUser.getUsername());
        response.put("phone", savedUser.getPhone());
        response.put("role", savedUser.getRole());

        return ResponseEntity.ok(response);
    }

    // Customer / Provider login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {

        String username = userService.normalizeUsername(user.getUsername());

        if (username == null || user.getPassword() == null) {
            return ResponseEntity.badRequest()
                .body("Username and password are required");
        }

        return userService.findByUsername(username)
                .map(existingUser -> {

                    if (!existingUser.getPassword()
                            .equals(user.getPassword())) {

                        return ResponseEntity.badRequest()
                                .body("Invalid password");
                    }

                    Map<String, Object> response = new HashMap<>();

                    response.put("id", existingUser.getId());
                    response.put("name", existingUser.getName());
                    response.put("username", existingUser.getUsername());
                    response.put("phone", existingUser.getPhone());
                    response.put("role", existingUser.getRole());

                    return ResponseEntity.ok(response);
                })
                .orElse(
                        ResponseEntity.badRequest()
                                .body("User not found")
                );
    }

    // Separate service-provider registration
    @PostMapping("/provider-register")
    public ResponseEntity<?> registerProvider(
            @RequestBody Map<String, Object> data) {

        String name = value(data, "name");
        String username = userService.normalizeUsername(
            value(data, "username"));
        String password = value(data, "password");
        String phone = value(data, "phone");
        String businessName = value(data, "businessName");
        String category = value(data, "category");
        String description = value(data, "description");
        String location = value(data, "location");

        Integer experience = 0;

        if (data.get("experience") != null) {
            experience = Integer.parseInt(
                    String.valueOf(data.get("experience"))
            );
        }

        if (username == null
            || username.isBlank()
            || password == null
            || password.isBlank()
            || name == null
            || name.isBlank()
            || businessName == null
            || businessName.isBlank()
            || category == null
            || category.isBlank()) {

            return ResponseEntity.badRequest()
                    .body("Please fill all required fields");
        }

        if (!username.matches("[a-z0-9][a-z0-9._-]{2,29}")) {
            return ResponseEntity.badRequest()
                .body("Username must be 3-30 characters using letters, numbers, '.', '_' or '-'");
        }

        if (userService.usernameExists(username)) {
            return ResponseEntity.badRequest()
                .body("Username already taken");
        }

        // Create provider's user account
        User user = new User();

        user.setName(name);
        user.setUsername(username);
        user.setPassword(password);
        user.setPhone(phone);
        user.setRole("PROVIDER");

        User savedUser = userService.registerUser(user);

        // Create provider profile
        ServiceProvider provider = new ServiceProvider();

        provider.setUserId(savedUser.getId());
        provider.setBusinessName(businessName);
        provider.setCategory(category);
        provider.setDescription(description);
        provider.setLocation(location);
        provider.setExperience(experience);
        provider.setAvailable(true);
        provider.setEmergencyAvailable(true);

        ServiceProvider savedProvider =
                serviceProviderService.addProvider(provider);

        Map<String, Object> response = new HashMap<>();

        response.put("message",
                "Service provider registered successfully");

        response.put("userId", savedUser.getId());
        response.put("providerId", savedProvider.getId());
        response.put("name", savedUser.getName());
        response.put("username", savedUser.getUsername());
        response.put("businessName",
                savedProvider.getBusinessName());
        response.put("category",
                savedProvider.getCategory());
        response.put("role", savedUser.getRole());

        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDuplicateUsername() {
        return ResponseEntity.status(409).body("Username already taken");
    }

    private String value(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value == null ? null : String.valueOf(value).trim();
    }
@DeleteMapping("/{userId}")
public ResponseEntity<?> deleteAccount(@PathVariable Integer userId) {
    try {
        userService.deleteUser(userId);
        return ResponseEntity.ok("Account deleted successfully");
    } catch (Exception e) {
        return ResponseEntity.badRequest().body("Unable to delete account");
    }
}
}