//src/main/java/com/cloudflix/backend/repository/UserRepository.java
package com.cloudflix.backend.repository;

import com.cloudflix.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);
}