// src/main/java/com/cloudflix/backend/dto/response/UserSummaryAdminResponse.java
package com.cloudflix.backend.dto.response;

import com.cloudflix.backend.entity.Role;
import com.cloudflix.backend.entity.User;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public class UserSummaryAdminResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Set<String> roles;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin; // Placeholder - you might add this later if you track last login

    // --- Constructors ---
    public UserSummaryAdminResponse() {}

    public UserSummaryAdminResponse(Long id, String firstName, String lastName, String email, Set<String> roles, boolean active, LocalDateTime createdAt, LocalDateTime lastLogin) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.roles = roles;
        this.active = active;
        this.createdAt = createdAt;
        this.lastLogin = lastLogin; // Example, adapt if not used
    }

    // Static factory method
    public static UserSummaryAdminResponse fromEntity(User user) {
        if (user == null) {
            return null;
        }
        Set<String> roleNames = user.getRoles().stream()
                                    .map(role -> role.getName().name()) // ERole.name() gives the string "ROLE_USER"
                                    .collect(Collectors.toSet());
        return new UserSummaryAdminResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                roleNames,
                user.isActive(),
                user.getCreatedAt(),
                null // TODO: Populate lastLogin if you implement this feature
        );
    }

    // --- Getters ---
    public Long getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public Set<String> getRoles() { return roles; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getLastLogin() { return lastLogin; }

    // --- Setters ---
    public void setId(Long id) { this.id = id; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setEmail(String email) { this.email = email; }
    public void setRoles(Set<String> roles) { this.roles = roles; }
    public void setActive(boolean active) { this.active = active; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
}