package com.localservices.marketplace.service;

import com.localservices.marketplace.model.User;
import com.localservices.marketplace.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Locale;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User registerUser(User user) {
        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    public void prepareVerification(User user) {
        user.setEmailVerified(false);
        user.setVerificationToken(UUID.randomUUID().toString());
    }

    public String createPasswordResetToken(User user) {
        String token = UUID.randomUUID().toString();
        user.setPasswordResetToken(token);
        user.setPasswordResetExpiresAt(LocalDateTime.now().plusMinutes(30));
        userRepository.save(user);
        return token;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByVerificationToken(String token) {
        return userRepository.findByVerificationToken(token);
    }

    public Optional<User> findByPasswordResetToken(String token) {
        return userRepository.findByPasswordResetToken(token);
    }

    public String normalizeUsername(String username) {
        return username == null
                ? null
                : username.trim().toLowerCase(Locale.ROOT);
    }
public void deleteUser(Integer userId) {
    if (!userRepository.existsById(userId)) {
        throw new RuntimeException("User not found");
    }

    userRepository.deleteById(userId);
}
}
