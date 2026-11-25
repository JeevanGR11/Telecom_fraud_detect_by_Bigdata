package com.telecom.service;

import com.telecom.dto.AuthDTOs.*;
import com.telecom.entity.AuditLog;
import com.telecom.entity.User;
import com.telecom.repository.AuditLogRepository;
import com.telecom.repository.UserRepository;
import com.telecom.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuditLogRepository auditLogRepository;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       AuditLogRepository auditLogRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.auditLogRepository = auditLogRepository;
    }

    public AuthResponse login(LoginRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty() ||
            request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return new AuthResponse(false, "Username and password are required.", null, null);
        }

        String username = request.getUsername().trim();
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            return new AuthResponse(false, "Invalid username or password.", null, null);
        }

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            return new AuthResponse(false, "Your account has been deactivated. Please contact an administrator.", null, null);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            return new AuthResponse(false, "Invalid username or password.", null, null);
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        UserDTO userDTO = new UserDTO(user.getId(), user.getUsername(), user.getEmail(), user.getFullName(), user.getRole(), user.getStatus());

        auditLogRepository.save(new AuditLog(user.getUsername(), "LOGIN_SUCCESS", "User authenticated successfully", "127.0.0.1"));

        return new AuthResponse(true, "Authentication successful. Welcome to TELECOM!", token, userDTO);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
            return new AuthResponse(false, "Full Name is required.", null, null);
        }
        if (request.getUsername() == null || request.getUsername().trim().length() < 3) {
            return new AuthResponse(false, "Username must be at least 3 characters.", null, null);
        }
        if (request.getEmail() == null || !EMAIL_PATTERN.matcher(request.getEmail().trim()).matches()) {
            return new AuthResponse(false, "Please provide a valid email address.", null, null);
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            return new AuthResponse(false, "Password must be at least 6 characters long.", null, null);
        }
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return new AuthResponse(false, "Passwords do not match.", null, null);
        }

        String username = request.getUsername().trim();
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByUsername(username)) {
            return new AuthResponse(false, "Username '" + username + "' is already taken.", null, null);
        }

        if (userRepository.existsByEmail(email)) {
            return new AuthResponse(false, "Email '" + email + "' is already registered.", null, null);
        }

        String assignedRole = "USER";
        if ("ADMIN".equalsIgnoreCase(request.getRole())) {
            // Check if there are any existing admins, else assign USER
            assignedRole = userRepository.count() == 0 ? "ADMIN" : "USER";
        }

        String passwordHash = passwordEncoder.encode(request.getPassword());
        User newUser = new User(username, email, passwordHash, request.getFullName().trim(), assignedRole);
        newUser = userRepository.save(newUser);

        auditLogRepository.save(new AuditLog(username, "USER_REGISTERED", "New account registered with role " + assignedRole, "127.0.0.1"));

        String token = jwtUtil.generateToken(newUser.getUsername(), newUser.getRole());
        UserDTO userDTO = new UserDTO(newUser.getId(), newUser.getUsername(), newUser.getEmail(), newUser.getFullName(), newUser.getRole(), newUser.getStatus());

        return new AuthResponse(true, "Registration successful! Welcome to TELECOM.", token, userDTO);
    }

    public UserDTO getCurrentUser(String username) {
        return userRepository.findByUsername(username)
                .map(u -> new UserDTO(u.getId(), u.getUsername(), u.getEmail(), u.getFullName(), u.getRole(), u.getStatus()))
                .orElse(null);
    }
}
