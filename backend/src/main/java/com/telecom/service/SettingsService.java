package com.telecom.service;

import com.telecom.entity.SystemSetting;
import com.telecom.entity.User;
import com.telecom.repository.SystemSettingRepository;
import com.telecom.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SettingsService {

    private final SystemSettingRepository settingRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SettingsService(SystemSettingRepository settingRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.settingRepository = settingRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Map<String, String> getAllSettings() {
        List<SystemSetting> settings = settingRepository.findAll();
        Map<String, String> map = new HashMap<>();
        for (SystemSetting s : settings) {
            map.put(s.getSettingKey(), s.getSettingValue());
        }
        return map;
    }

    @Transactional
    public void updateSettings(Map<String, String> updates) {
        for (Map.Entry<String, String> entry : updates.entrySet()) {
            SystemSetting setting = settingRepository.findBySettingKey(entry.getKey())
                    .orElse(new SystemSetting(entry.getKey(), entry.getValue(), "Custom Setting"));
            setting.setSettingValue(entry.getValue());
            settingRepository.save(setting);
        }
    }

    @Transactional
    public boolean changePassword(String username, String currentPassword, String newPassword) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return false;

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            return false;
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }
}
