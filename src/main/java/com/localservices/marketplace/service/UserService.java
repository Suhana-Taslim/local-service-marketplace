package com.localservices.marketplace.service;

import com.localservices.marketplace.model.User;
import com.localservices.marketplace.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Locale;

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
