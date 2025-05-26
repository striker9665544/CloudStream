package com.cloudflix.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate; // Ensure this import
import java.util.Set;

import com.cloudflix.backend.entity.Role;

@Data
public class SignUpRequest {
    @NotBlank
    @Size(min = 2, max = 100)
    private String firstName;

    @Size(max = 100)
    private String middleName; // Optional

    @NotBlank
    @Size(min = 2, max = 100)
    private String lastName;

    @NotBlank
    @Size(max = 255)
    @Email
    private String email;

    @NotBlank
    @Size(min = 6, max = 40)
    private String password;

    private LocalDate dateOfBirth; // Optional, can be null


    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    // Be cautious with password setters in real applications -
    // you typically handle password hashing here or in a service layer.
    public void setPassword(String password) {
        this.password = password;
    }

    // Assuming you have dateOfBirth
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

}