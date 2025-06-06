// src/main/java/com/cloudflix/backend/dto/request/UserRoleUpdateRequest.java
package com.cloudflix.backend.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

public class UserRoleUpdateRequest {

    @NotEmpty(message = "Roles set cannot be empty. To remove all roles, provide an empty set (though not recommended for users).")
    private Set<String> roles; // e.g., ["ROLE_USER", "ROLE_UPLOADER"]

    // --- Constructors ---
    public UserRoleUpdateRequest() {}

    public UserRoleUpdateRequest(Set<String> roles) {
        this.roles = roles;
    }

    // --- Getter & Setter ---
    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }
}