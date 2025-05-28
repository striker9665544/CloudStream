//src/main/java/com/cloudflix/backend/entity/SubscriptionPlan.java
package com.cloudflix.backend.entity;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "subscription_plans")
@NoArgsConstructor
public class SubscriptionPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Changed to Long to match your schema

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 3)
    private String currency = "IND";

    @Column(nullable = false)
    private Integer durationDays;

    @Lob // For potentially longer text
    private String description;
    
    
    public BigDecimal getPrice() { // Common getter name for the token type
        return price;
    }

    public Long getId() {
        return id;
    }

    public String getCurrency() {
        return currency;
    }

    public String getName() {
        return name;
    }

    public Integer getDurationDays() {
        return durationDays;
    }
    
    public void getDescription(String description) {
        this.description = description;
    }

    // --- Setter Methods ---
    // Note: Setters might not be needed for all fields in a response DTO,
    // as it's often immutable after creation. However, they are provided below
    // as requested for all fields.

    public void setAccessToken(BigDecimal price) { // Setter name matching getter
        this.price = price;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDurationDays(Integer durationDays) {
        this.durationDays = durationDays;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public void setCurrency(String currency) {
    	this.currency = currency;
    }
    
    
    public SubscriptionPlan(String name, BigDecimal price, String currency, Integer durationDays, String description) {
        this.name = name;
        this.price = price;
        this.currency = currency; // Explicitly setting currency allows overriding the default
        this.durationDays = durationDays;
        this.description = description;
    }
    
    public SubscriptionPlan() {
        // Default values for fields like currency are often set directly
        // or within a default constructor if not handled by the database schema.
        // If using the field default `private String currency = "IND";`,
        // you don't explicitly need to set it here unless you need conditional logic.
    }
}