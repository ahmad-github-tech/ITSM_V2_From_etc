package com.supportflow.controller;

import com.supportflow.entity.SystemSetting;
import com.supportflow.repository.SystemSettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
@CrossOrigin(origins = "*")
public class SystemSettingController {

    @Autowired
    private SystemSettingRepository systemSettingRepository;

    @GetMapping("/{key}")
    public SystemSetting getSetting(@PathVariable String key) {
        return systemSettingRepository.findById(key)
                .orElseGet(() -> new SystemSetting(key, "{}"));
    }

    @PostMapping("/{key}")
    public SystemSetting saveSetting(@PathVariable String key, @RequestBody SystemSetting setting) {
        setting.setSettingKey(key);
        return systemSettingRepository.save(setting);
    }
}
