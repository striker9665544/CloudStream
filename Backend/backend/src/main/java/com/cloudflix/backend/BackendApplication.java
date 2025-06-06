//src/main/java/com/cloudflix/backend/BackendApplication.java
package com.cloudflix.backend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.cloudflix.backend.entity.ERole;
import com.cloudflix.backend.entity.Role;
import com.cloudflix.backend.repository.RoleRepository;

@SpringBootApplication
public class BackendApplication {
	
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args); // Use THIS class
    }

    // This bean will run once the application context is loaded.
    // You can keep this here or move it to a @Configuration class
    @Bean
    CommandLineRunner initRoles(RoleRepository roleRepository) {
        return args -> {
            if (roleRepository.findByName(ERole.ROLE_USER).isEmpty()) {
                roleRepository.save(new Role(ERole.ROLE_USER));
                System.out.println("ROLE_USER initialized.");
            }
            if (roleRepository.findByName(ERole.ROLE_ADMIN).isEmpty()) {
                roleRepository.save(new Role(ERole.ROLE_ADMIN));
                System.out.println("ROLE_ADMIN initialized.");
            }
            if (roleRepository.findByName(ERole.ROLE_UPLOADER).isEmpty()) {
                roleRepository.save(new Role(ERole.ROLE_UPLOADER));
                System.out.println("ROLE_UPLOADER initialized.");
            }
        };
    }
}