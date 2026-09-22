package com.localservices.marketplace.controller;

import com.localservices.marketplace.model.ServiceProvider;
import com.localservices.marketplace.model.User;
import com.localservices.marketplace.service.ServiceProviderService;
import com.localservices.marketplace.service.UserService;
import org.springframework.http.ResponseEntity;
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

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            return ResponseEntity.badRequest()
                    .body("Email is required");
        }

        if (userService.emailExists(user.getEmail())) {
            return ResponseEntity.badRequest()
                    .body("Email already registered");
        }

        user.setRole("CUSTOMER");

        User savedUser = userService.registerUser(user);

        Map<String, Object> response = new HashMap<>();
        response.put("id", savedUser.getId());
        response.put("name", savedUser.getName());
        response.put("email", savedUser.getEmail());
        response.put("phone", savedUser.getPhone());
        response.put("role", savedUser.getRole());

        return ResponseEntity.ok(response);
    }

    // Customer / Provider login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {

        if (user.getEmail() == null || user.getPassword() == null) {
            return ResponseEntity.badRequest()
                    .body("Email and password are required");
        }

        return userService.findByEmail(user.getEmail())
                .map(existingUser -> {

                    if (!existingUser.getPassword()
                            .equals(user.getPassword())) {

                        return ResponseEntity.badRequest()
                                .body("Invalid password");
                    }

                    Map<String, Object> response = new HashMap<>();

                    response.put("id", existingUser.getId());
                    response.put("name", existingUser.getName());
                    response.put("email", existingUser.getEmail());
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

        String name = String.valueOf(data.get("name"));
        String email = String.valueOf(data.get("email"));
        String password = String.valueOf(data.get("password"));
        String phone = String.valueOf(data.get("phone"));
        String businessName = String.valueOf(data.get("businessName"));
        String category = String.valueOf(data.get("category"));
        String description = String.valueOf(data.get("description"));
        String location = String.valueOf(data.get("location"));

        Integer experience = 0;

        if (data.get("experience") != null) {
            experience = Integer.parseInt(
                    String.valueOf(data.get("experience"))
            );
        }

        if (email.isBlank()
                || password.isBlank()
                || name.isBlank()
                || businessName.isBlank()
                || category.isBlank()) {

            return ResponseEntity.badRequest()
                    .body("Please fill all required fields");
        }

        if (userService.emailExists(email)) {
            return ResponseEntity.badRequest()
                    .body("Email already registered");
        }

        // Create provider's user account
        User user = new User();

        user.setName(name);
        user.setEmail(email);
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
        response.put("businessName",
                savedProvider.getBusinessName());
        response.put("category",
                savedProvider.getCategory());
        response.put("role", savedUser.getRole());

        return ResponseEntity.ok(response);
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