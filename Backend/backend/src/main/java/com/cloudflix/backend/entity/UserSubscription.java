package com.cloudflix.backend.entity;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_subscriptions")
@NoArgsConstructor
public class UserSubscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlan plan;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false, length = 50)
    private String status; // e.g., "ACTIVE", "CANCELLED", "EXPIRED"

    private Boolean autoRenew = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now(); // Handled by prePersist or default
    
    // --- Getter and Setter Methods ---

    public Long getId() {
        return id;
    }

    // Typically, the ID setter is not used for database-generated IDs,
    // but included for completeness.
    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public SubscriptionPlan getPlan() {
        return plan;
    }

    public void setPlan(SubscriptionPlan plan) {
        this.plan = plan;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getAutoRenew() {
        return autoRenew;
    }

    public void setAutoRenew(Boolean autoRenew) {
        this.autoRenew = autoRenew;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // The createdAt field has `updatable = false`, meaning its setter
    // should typically not be used or perhaps not even exist if strict
    // immutability is desired after creation. However, here's the setter
    // as requested for all fields.
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // --- Constructors (Add if needed, e.g., NoArgsConstructor, AllArgsConstructor, etc.) ---

    // @NoArgsConstructor // Example using Lombok
    // @AllArgsConstructor // Example using Lombok

    // Or manual constructors:
    public UserSubscription() {}

    public UserSubscription(User user, SubscriptionPlan plan, LocalDate startDate, LocalDate endDate, String status) {
         this.user = user;
         this.plan = plan;
         this.startDate = startDate;
         this.endDate = endDate;
         this.status = status;
    }

    // --- Other methods (like toString(), equals(), hashCode()) ---

    // @Override // Example using Lombok @Data or manual implementation
    // public String toString() { ... }
}