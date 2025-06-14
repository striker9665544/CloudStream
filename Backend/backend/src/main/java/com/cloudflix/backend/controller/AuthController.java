//src/main/java/com/cloudflix/backend/controller/AuthController.java
package com.cloudflix.backend.controller;

import com.cloudflix.backend.dto.request.LoginRequest;
import com.cloudflix.backend.dto.request.SignUpRequest;
import com.cloudflix.backend.dto.response.JwtResponse;
import com.cloudflix.backend.dto.response.MessageResponse;
import com.cloudflix.backend.entity.ERole;
import com.cloudflix.backend.entity.Role;
import com.cloudflix.backend.entity.User;
import com.cloudflix.backend.repository.RoleRepository;
import com.cloudflix.backend.repository.UserRepository;
import com.cloudflix.backend.security.jwt.JwtUtils;
import com.cloudflix.backend.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600) // Or configure globally in WebSecurityConfig
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping(value = "/signin", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt,
                                                 userDetails.getId(),
                                                 userDetails.getUsername(), // This is the email
                                                 userDetails.getFirstName(),
                                                 roles));
    }

    @PostMapping(value = "/signup", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new user's account
        User user = new User(signUpRequest.getFirstName(),
                             signUpRequest.getMiddleName(),
                             signUpRequest.getLastName(),
                             signUpRequest.getEmail(),
                             encoder.encode(signUpRequest.getPassword()),
                             signUpRequest.getDateOfBirth());

        Set<Role> roles = new HashSet<>();
        // By default, assign ROLE_USER. You can make this more dynamic.
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Role USER is not found."));
        roles.add(userRole);
        
        // For testing, you might want to create an admin user
        // if (signUpRequest.getEmail().contains("admin")) {
        //    Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
        //            .orElseThrow(() -> new RuntimeException("Error: Role ADMIN is not found."));
        //    roles.add(adminRole);
        // }


        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
    
    @GetMapping(value = "/api/test-msg", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> testMessageMap() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Test successful as map");
        return ResponseEntity.ok(response);
    }

    // You might want a utility to create initial roles if they don't exist
    // Call this once at application startup or manually
    // For example, in your main application class using CommandLineRunner
    // @Bean
    // CommandLineRunner initRoles(RoleRepository roleRepository) {
    //    return args -> {
    //        if (roleRepository.findByName(ERole.ROLE_USER).isEmpty()) {
    //            roleRepository.save(new Role(ERole.ROLE_USER));
    //        }
    //        if (roleRepository.findByName(ERole.ROLE_ADMIN).isEmpty()) {
    //            roleRepository.save(new Role(ERole.ROLE_ADMIN));
    //        }
    //    };
    // }
}