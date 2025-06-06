// src/main/java/com/cloudflix/backend/controller/AdminUserController.java
package com.cloudflix.backend.controller;

import com.cloudflix.backend.dto.request.UserRoleUpdateRequest;
import com.cloudflix.backend.dto.request.UserStatusUpdateRequest;
import com.cloudflix.backend.dto.response.MessageResponse;
import com.cloudflix.backend.dto.response.UserDetailsAdminResponse;
import com.cloudflix.backend.dto.response.UserSummaryAdminResponse;
import com.cloudflix.backend.service.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')") // All endpoints in this controller require ADMIN role
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;

    // Get a paginated list of all users
    @GetMapping
    public ResponseEntity<Page<UserSummaryAdminResponse>> getAllUsers(
            @PageableDefault(size = 20, sort = "createdAt,desc") Pageable pageable) {
        Page<UserSummaryAdminResponse> usersPage = adminUserService.getAllUsers(pageable);
        return ResponseEntity.ok(usersPage);
    }

    // Get detailed information about a specific user
    @GetMapping("/{userId}")
    public ResponseEntity<UserDetailsAdminResponse> getUserById(@PathVariable Long userId) {
        UserDetailsAdminResponse userDetails = adminUserService.getUserByIdForAdmin(userId);
        return ResponseEntity.ok(userDetails);
    }

    // Update roles for a specific user
    @PutMapping("/{userId}/roles")
    public ResponseEntity<UserDetailsAdminResponse> updateUserRoles(
            @PathVariable Long userId,
            @Valid @RequestBody UserRoleUpdateRequest roleUpdateRequest) {
        UserDetailsAdminResponse updatedUser = adminUserService.updateUserRoles(userId, roleUpdateRequest);
        return ResponseEntity.ok(updatedUser);
    }

    // Update the active status of a specific user
    @PatchMapping("/{userId}/status")
    public ResponseEntity<UserDetailsAdminResponse> updateUserActiveStatus(
            @PathVariable Long userId,
            @Valid @RequestBody UserStatusUpdateRequest statusUpdateRequest) {
        UserDetailsAdminResponse updatedUser = adminUserService.setUserActiveStatus(userId, statusUpdateRequest.isActive());
        return ResponseEntity.ok(updatedUser);
    }

    // Optional: Endpoint for an admin to physically delete a user
    /*
    @DeleteMapping("/{userId}")
    public ResponseEntity<MessageResponse> deleteUser(@PathVariable Long userId) {
        adminUserService.deleteUser(userId); // Assuming this method exists in AdminUserService
        return ResponseEntity.ok(new MessageResponse("User with ID " + userId + " deleted successfully."));
    }
    */
}