//src/main/java/com/cloudflix/backend/entity/Role.java
package com.cloudflix.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
// import lombok.Data; // Comment out if adding manually
import lombok.NoArgsConstructor;

@Entity
@Table(name = "roles")
@Data // Comment out if adding manually
@NoArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, unique = true)
    private ERole name;

    public Role() {
    	
    }
    
    public Role(ERole name) {
        this.name = name;
    }

    // Manually added getter
    public ERole getName() {
        return this.name;
    }

    // Manually added setter (if needed elsewhere, though @Data provides it)
    public void setName(ERole name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}