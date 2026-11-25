package com.telecom.controller;

import com.telecom.service.SettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final SettingsService settingsService;

    public SettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping
    public ResponseEntity<Map<String, String>> getSettings() {
        return ResponseEntity.ok(settingsService.getAllSettings());
    }

    @PutMapping
    public ResponseEntity<?> updateSettings(@RequestBody Map<String, String> updates) {
        settingsService.updateSettings(updates);
        return ResponseEntity.ok(Map.of("success", true, "message", "Settings updated successfully."));
    }

    @PostMapping("/password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> body,
                                            Authentication authentication) {
        String currentPassword = body.get("currentPassword");
        String newPassword = body.get("newPassword");
        String username = authentication != null ? authentication.getName() : "admin";

        if (newPassword == null || newPassword.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("error", "New password must be at least 6 characters long."));
        }

        boolean success = settingsService.changePassword(username, currentPassword, newPassword);
        if (!success) {
            return ResponseEntity.badRequest().body(Map.of("error", "Current password is incorrect."));
        }

        return ResponseEntity.ok(Map.of("success", true, "message", "Password changed successfully."));
    }
}
