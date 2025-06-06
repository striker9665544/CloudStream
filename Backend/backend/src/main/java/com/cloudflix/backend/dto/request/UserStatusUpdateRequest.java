// src/main/java/com/cloudflix/backend/dto/request/UserStatusUpdateRequest.java
package com.cloudflix.backend.dto.request;

import jakarta.validation.constraints.NotNull;

public class UserStatusUpdateRequest {

    @NotNull(message = "Active status cannot be null.")
    private Boolean active;

    // --- Constructors ---
    public UserStatusUpdateRequest() {}

    public UserStatusUpdateRequest(Boolean active) {
        this.active = active;
    }

    // --- Getter & Setter ---
    public Boolean isActive() { return active; } // Getter for boolean often uses "is" prefix
    public void setActive(Boolean active) { this.active = active; }
}