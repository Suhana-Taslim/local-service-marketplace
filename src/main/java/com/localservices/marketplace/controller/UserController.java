package com.localservices.marketplace.controller;

import com.localservices.marketplace.model.ServiceProvider;
import com.localservices.marketplace.model.User;
import com.localservices.marketplace.service.ServiceProviderService;
import com.localservices.marketplace.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {

    private final UserService userService;
    private final ServiceProviderService serviceProviderService;
    private final JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    public UserController(
            UserService userService,
            ServiceProviderService serviceProviderService,
            JavaMailSender mailSender) {

        this.userService = userService;
        this.serviceProviderService = serviceProviderService;
        this.mailSender = mailSender;
    }

    // Customer registration
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {

        String username = userService.normalizeUsername(user.getUsername());
        String email = userService.normalizeEmail(user.getEmail());

        if (username == null || username.isBlank()
            || email == null || email.isBlank()
            || user.getName() == null || user.getName().isBlank()
            || user.getPassword() == null || user.getPassword().length() < 8) {
            return ResponseEntity.badRequest()
            .body("Name, username, email, and a password of at least 8 characters are required");
        }

        if (!username.matches("[a-z0-9][a-z0-9._-]{2,29}")) {
            return ResponseEntity.badRequest()
                .body("Username must be 3-30 characters using letters, numbers, '.', '_' or '-'");
        }

        user.setUsername(username);
        user.setEmail(email);

        if (userService.usernameExists(username) || userService.emailExists(email)) {
            return ResponseEntity.badRequest()
            .body("Username or email is already registered");
        }

        user.setRole("CUSTOMER");
        userService.prepareVerification(user);

        User savedUser = userService.registerUser(user);
        sendVerificationEmail(savedUser);

        Map<String, Object> response = new HashMap<>();
        response.put("id", savedUser.getId());
        response.put("name", savedUser.getName());
        response.put("username", savedUser.getUsername());
        response.put("email", savedUser.getEmail());
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

                if (!existingUser.isEmailVerified()) {
                return ResponseEntity.badRequest()
                    .body("Please verify your email before logging in");
                }

                    if (!existingUser.getPassword()
                            .equals(user.getPassword())) {

                        return ResponseEntity.badRequest()
                                .body("Invalid password");
                    }

                    Map<String, Object> response = new HashMap<>();

                    response.put("id", existingUser.getId());
                    response.put("name", existingUser.getName());
                    response.put("username", existingUser.getUsername());
                    response.put("email", existingUser.getEmail());
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
        String email = userService.normalizeEmail(value(data, "email"));
        String password = value(data, "password");
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
            || email == null
            || email.isBlank()
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

        if (userService.usernameExists(username) || userService.emailExists(email)) {
            return ResponseEntity.badRequest()
            .body("Username or email is already registered");
        }

        // Create provider's user account
        User user = new User();

        user.setName(name);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole("PROVIDER");
        userService.prepareVerification(user);

        User savedUser = userService.registerUser(user);
        sendVerificationEmail(savedUser);

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
        return ResponseEntity.status(409).body("Username or email is already registered");
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        return userService.findByVerificationToken(token)
                .map(user -> {
                    user.setEmailVerified(true);
                    user.setVerificationToken(null);
                    userService.registerUser(user);
                    return ResponseEntity.ok("Email verified. You can now log in.");
                })
                .orElse(ResponseEntity.badRequest().body("Invalid verification link"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> data) {
        String username = userService.normalizeUsername(data.get("username"));
        String email = userService.normalizeEmail(data.get("email"));

        return userService.findByUsername(username)
                .filter(user -> user.getEmail().equals(email) && user.isEmailVerified())
                .map(user -> {
                    String token = userService.createPasswordResetToken(user);
                    sendPasswordResetEmail(user, token);
                    return ResponseEntity.ok("If the details match, a password reset email has been sent.");
                })
                .orElse(ResponseEntity.ok("If the details match, a password reset email has been sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> data) {
        String token = data.get("token");
        String password = data.get("password");
        return userService.findByPasswordResetToken(token)
                .filter(user -> user.getPasswordResetExpiresAt() != null
                        && user.getPasswordResetExpiresAt().isAfter(LocalDateTime.now()))
                .map(user -> {
                    user.setPassword(password);
                    user.setPasswordResetToken(null);
                    user.setPasswordResetExpiresAt(null);
                    userService.registerUser(user);
                    return ResponseEntity.ok("Password reset successfully");
                })
                .orElse(ResponseEntity.badRequest().body("Invalid or expired reset link"));
    }

    private void sendVerificationEmail(User user) {
        sendEmail(user.getEmail(), "Verify your Local Services account",
                        "Verify your email: " + baseUrl + "/api/users/verify?token="
                        + user.getVerificationToken());
    }

    private void sendPasswordResetEmail(User user, String token) {
        sendEmail(user.getEmail(), "Reset your Local Services password",
                "Reset your password: " + baseUrl + "/reset-password.html?token=" + token);
    }

    private void sendEmail(String recipient, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipient);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
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