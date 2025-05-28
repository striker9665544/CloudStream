//src/main/java/com/cloudflix/backend/dto/response/JwtResponse.java
package com.cloudflix.backend.dto.response;

import java.util.List;
import java.util.List;

public class JwtResponse { // Assuming the class name is JwtResponse

    private String token;
    private String type = "Bearer"; // Default value
    private Long id;
    private String email;
    private String firstName;
    private List<String> roles;

    // Constructor provided by user
    public JwtResponse(String accessToken, Long id, String email, String firstName, List<String> roles) {
        this.token = accessToken; // Mapping accessToken parameter to token field
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.roles = roles;
    }

    // --- Getter Methods ---

    public String getAccessToken() { // Common getter name for the JWT token
        return token;
    }

    public String getTokenType() { // Common getter name for the token type
        return type;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public List<String> getRoles() {
        return roles;
    }

    // --- Setter Methods ---
    // Note: Setters might not be needed for all fields in a response DTO,
    // as it's often immutable after creation. However, they are provided below
    // as requested for all fields.

    public void setAccessToken(String token) { // Setter name matching getter
        this.token = token;
    }

    public void setTokenType(String type) { // Setter name matching getter
        this.type = type;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    // You might also want to add a no-argument constructor if needed elsewhere
    // public JwtResponse() {}

    // You might also want to add toString(), equals(), hashCode() methods.
}
