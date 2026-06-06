package com.supportflow.controller;

import com.supportflow.entity.User;
import com.supportflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @GetMapping("/debug-columns")
    public Object debugColumns() {
        java.util.Map<String, Object> debugInfo = new java.util.HashMap<>();
        try {
            debugInfo.put("columns", jdbcTemplate.queryForList("SHOW COLUMNS FROM users"));
            debugInfo.put("raw_rows", jdbcTemplate.queryForList("SELECT * FROM users"));
            debugInfo.put("jpa_rows", userRepository.findAll());
            debugInfo.put("jpa_rows_count", userRepository.count());
        } catch (Exception e) {
            debugInfo.put("error", e.getMessage());
        }
        return debugInfo;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        StringBuilder debugLog = new StringBuilder();
        debugLog.append("--- CREATE USER TRANSACTION ---\n");
        debugLog.append("Incoming payload: \n")
                .append("  ID: ").append(user.getId()).append("\n")
                .append("  Name: ").append(user.getName()).append("\n")
                .append("  Email: ").append(user.getEmail()).append("\n")
                .append("  Mobile: ").append(user.getMobile()).append("\n");

        User savedUser = null;
        try {
            savedUser = userRepository.save(user);
            debugLog.append("Saved Successfully: ID=").append(savedUser.getId()).append("\n");
        } catch (Exception e) {
            debugLog.append("ERROR: ").append(e.getMessage()).append("\n");
            java.io.StringWriter sw = new java.io.StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(sw);
            e.printStackTrace(pw);
            debugLog.append(sw.toString()).append("\n");
        }

        try {
            java.io.FileWriter writer = new java.io.FileWriter("user_update_debug.txt", true);
            writer.write(debugLog.toString());
            writer.write("\n\n");
            writer.close();
        } catch (Exception ignored) {}

        if (savedUser == null) {
            throw new RuntimeException("Creation failed. See logs.");
        }
        return savedUser;
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable String id, @RequestBody User userDetails) {
        StringBuilder debugLog = new StringBuilder();
        debugLog.append("--- UPDATE USER TRANSACTION ---\n");
        debugLog.append("Path Variable ID: ").append(id).append("\n");
        debugLog.append("Incoming payload: \n")
                .append("  ID: ").append(userDetails.getId()).append("\n")
                .append("  Name: ").append(userDetails.getName()).append("\n")
                .append("  Email: ").append(userDetails.getEmail()).append("\n")
                .append("  Mobile: ").append(userDetails.getMobile()).append("\n")
                .append("  GatewayActiveNotify: ").append(userDetails.getGatewayActiveNotify()).append("\n");

        User savedUser = null;
        try {
            User user = userRepository.findById(id)
                    .orElseGet(() -> userRepository.findAll().stream()
                            .filter(u -> u.getId().equalsIgnoreCase(id))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("User not found: " + id)));
            user.setName(userDetails.getName());
            user.setRole(userDetails.getRole());
            user.setStatus(userDetails.getStatus());
            if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
                user.setPassword(userDetails.getPassword());
            }
            if (userDetails.getRecoveryQuestion() != null) {
                user.setRecoveryQuestion(userDetails.getRecoveryQuestion());
            }
            if (userDetails.getRecoveryAnswer() != null) {
                user.setRecoveryAnswer(userDetails.getRecoveryAnswer());
            }
            
            // Set email and mobile fields explicitly
            user.setEmail(userDetails.getEmail());
            user.setMobile(userDetails.getMobile());
            
            if (userDetails.getGatewayActiveNotify() != null) {
                user.setGatewayActiveNotify(userDetails.getGatewayActiveNotify());
            }

            debugLog.append("Before Save User: \n")
                    .append("  ID: ").append(user.getId()).append("\n")
                    .append("  Name: ").append(user.getName()).append("\n")
                    .append("  Email: ").append(user.getEmail()).append("\n")
                    .append("  Mobile: ").append(user.getMobile()).append("\n");

            savedUser = userRepository.save(user);

            debugLog.append("After Save User (Saved Successfully): \n")
                    .append("  ID: ").append(savedUser.getId()).append("\n")
                    .append("  Name: ").append(savedUser.getName()).append("\n")
                    .append("  Email: ").append(savedUser.getEmail()).append("\n")
                    .append("  Mobile: ").append(savedUser.getMobile()).append("\n");

        } catch (Exception e) {
            debugLog.append("ERROR: ").append(e.getMessage()).append("\n");
            // print full stack trace to string
            java.io.StringWriter sw = new java.io.StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(sw);
            e.printStackTrace(pw);
            debugLog.append(sw.toString()).append("\n");
        }

        try {
            java.io.FileWriter writer = new java.io.FileWriter("user_update_debug.txt", true);
            writer.write(debugLog.toString());
            writer.write("\n\n");
            writer.close();
        } catch (Exception ignored) {}

        if (savedUser == null) {
            throw new RuntimeException("Update failed. See logs.");
        }
        return savedUser;
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable String id) {
        userRepository.deleteById(id);
    }
}
