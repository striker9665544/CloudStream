// src/main/java/com/cloudflix/backend/service/AdminUserService.java
package com.cloudflix.backend.service;

import com.cloudflix.backend.dto.request.UserRoleUpdateRequest;
import com.cloudflix.backend.dto.response.UserDetailsAdminResponse;
import com.cloudflix.backend.dto.response.UserSummaryAdminResponse;
import com.cloudflix.backend.entity.ERole;
import com.cloudflix.backend.entity.Role;
import com.cloudflix.backend.entity.User;
import com.cloudflix.backend.exception.ResourceNotFoundException;
import com.cloudflix.backend.repository.RoleRepository;
import com.cloudflix.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize; // For service-level security
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@PreAuthorize("hasRole('ADMIN')") // Secure all methods in this service for ADMIN role by default
public class AdminUserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Transactional(readOnly = true)
    public Page<UserSummaryAdminResponse> getAllUsers(Pageable pageable) {
        Page<User> usersPage = userRepository.findAll(pageable);
        return usersPage.map(UserSummaryAdminResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public UserDetailsAdminResponse getUserByIdForAdmin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return UserDetailsAdminResponse.fromEntity(user);
    }

    @Transactional
    public UserDetailsAdminResponse updateUserRoles(Long userId, UserRoleUpdateRequest roleUpdateRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Set<Role> newRoles = new HashSet<>();
        if (roleUpdateRequest.getRoles() != null) {
            for (String roleNameStr : roleUpdateRequest.getRoles()) {
                try {
                    ERole eRole = ERole.valueOf(roleNameStr.toUpperCase()); // Ensure it matches enum values
                    Role role = roleRepository.findByName(eRole)
                            .orElseThrow(() -> new ResourceNotFoundException("Role", "name", roleNameStr));
                    newRoles.add(role);
                } catch (IllegalArgumentException e) {
                    // Handle case where string doesn't match any ERole enum constant
                    throw new IllegalArgumentException("Invalid role name provided: " + roleNameStr);
                }
            }
        }
        
        // It's crucial that at least ROLE_USER is present if other roles are removed,
        // unless you specifically want to make a user have no roles (which might break login).
        // This logic might need refinement based on business rules.
        // For example, ensuring ROLE_USER is always present unless explicitly removing all roles for suspension.
        if (newRoles.isEmpty()) {
            // Optionally, re-add ROLE_USER by default if the set becomes empty to prevent users from having no roles
            // Role userRole = roleRepository.findByName(ERole.ROLE_USER)
            //        .orElseThrow(() -> new RuntimeException("Error: Default Role USER is not found."));
            // newRoles.add(userRole);
            // For now, we allow an empty set which means admin explicitly removed all roles.
        }

        user.setRoles(newRoles);
        User updatedUser = userRepository.save(user);
        return UserDetailsAdminResponse.fromEntity(updatedUser);
    }

    @Transactional
    public UserDetailsAdminResponse setUserActiveStatus(Long userId, boolean isActive) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Prevent deactivating the last admin if you have such a rule (more complex logic)
        // For now, admin can deactivate any user including themselves (potentially risky)

        user.setActive(isActive);
        User updatedUser = userRepository.save(user);
        return UserDetailsAdminResponse.fromEntity(updatedUser);
    }

    // Note: Physical deletion of users is often discouraged.
    // Deactivation (isActive = false) is usually preferred.
    // If actual deletion is needed:
    /*
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        // Add checks here: e.g., cannot delete the last admin user.
        userRepository.delete(user);
    }
    */
}