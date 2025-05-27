// src/main/java/com/cloudflix/backend/repository/TagRepository.java
package com.cloudflix.backend.repository;

import com.cloudflix.backend.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByNameIgnoreCase(String name);
    List<Tag> findByNameInIgnoreCase(Set<String> names);
}