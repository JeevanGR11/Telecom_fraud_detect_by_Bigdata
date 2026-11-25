package com.telecom.service;

import com.telecom.dto.AuthDTOs.UserDTO;
import com.telecom.entity.User;
import com.telecom.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(u -> new UserDTO(u.getId(), u.getUsername(), u.getEmail(), u.getFullName(), u.getRole(), u.getStatus()))
                .collect(Collectors.toList());
    }

    public UserDTO getUserById(Long id) {
        return userRepository.findById(id)
                .map(u -> new UserDTO(u.getId(), u.getUsername(), u.getEmail(), u.getFullName(), u.getRole(), u.getStatus()))
                .orElse(null);
    }

    @Transactional
    public UserDTO updateUserRole(Long id, String role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        if (!"ADMIN".equalsIgnoreCase(role) && !"USER".equalsIgnoreCase(role)) {
            throw new IllegalArgumentException("Invalid role. Role must be ADMIN or USER.");
        }

        user.setRole(role.toUpperCase());
        user = userRepository.save(user);
        return new UserDTO(user.getId(), user.getUsername(), user.getEmail(), user.getFullName(), user.getRole(), user.getStatus());
    }

    @Transactional
    public UserDTO updateUserStatus(Long id, String status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        if (!"ACTIVE".equalsIgnoreCase(status) && !"INACTIVE".equalsIgnoreCase(status)) {
            throw new IllegalArgumentException("Invalid status. Status must be ACTIVE or INACTIVE.");
        }

        user.setStatus(status.toUpperCase());
        user = userRepository.save(user);
        return new UserDTO(user.getId(), user.getUsername(), user.getEmail(), user.getFullName(), user.getRole(), user.getStatus());
    }
}
