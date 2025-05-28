//src/main/java/com/cloudflix/backend/repository/RoleRepository.java
package com.cloudflix.backend.repository;

import com.cloudflix.backend.entity.ERole;
import com.cloudflix.backend.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(ERole name);
}