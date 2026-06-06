package com.supportflow.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    private String role;
    
    private String status;
    
    private String password;
    
    private String recoveryQuestion;
    private String recoveryAnswer;

    @Column(name = "email", nullable = true)
    private String email;

    @Column(name = "mobile", nullable = true)
    private String mobile;

    @Column(name = "gateway_active_notify", nullable = true)
    private Boolean gatewayActiveNotify = true;

    public User() {}

    public User(String id, String name, String role, String status, String password) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.status = status;
        this.password = password;
        this.gatewayActiveNotify = true;
    }

    public User(String id, String name, String role, String status, String password, String email, String mobile, Boolean gatewayActiveNotify) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.status = status;
        this.password = password;
        this.email = email;
        this.mobile = mobile;
        this.gatewayActiveNotify = gatewayActiveNotify != null ? gatewayActiveNotify : true;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRecoveryQuestion() { return recoveryQuestion; }
    public void setRecoveryQuestion(String recoveryQuestion) { this.recoveryQuestion = recoveryQuestion; }

    public String getRecoveryAnswer() { return recoveryAnswer; }
    public void setRecoveryAnswer(String recoveryAnswer) { this.recoveryAnswer = recoveryAnswer; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public Boolean getGatewayActiveNotify() { return gatewayActiveNotify; }
    public void setGatewayActiveNotify(Boolean gatewayActiveNotify) { this.gatewayActiveNotify = gatewayActiveNotify != null ? gatewayActiveNotify : true; }
}
