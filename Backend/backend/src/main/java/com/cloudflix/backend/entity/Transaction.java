//src/main/java/com/cloudflix/backend/entity/Transaction.java
package com.cloudflix.backend.entity;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@NoArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_subscription_id") // Nullable if transaction not for subscription
    private UserSubscription userSubscription;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency = "USD";

    @Column(nullable = false)
    private LocalDateTime transactionDate = LocalDateTime.now();

    @Lob
    private String paymentMethodDetails; // "Test Card ****1234"

    @Column(nullable = false, length = 50)
    private String status; // "SUCCESS", "FAILED", "PENDING"

    private String externalTransactionId; // Dummy ID
    
    
    // --- Constructors ---

    // 1. No-Argument Constructor (Required by JPA)
    public Transaction() {
        // Default values for currency and transactionDate are set directly on the field
    }

    // 2. Constructor for creating a new transaction (excluding ID, transactionDate defaults)
    // Includes essential fields plus nullable userSubscription
    public Transaction(User user, UserSubscription userSubscription, BigDecimal amount, String currency, String status) {
        this.user = user;
        this.userSubscription = userSubscription; // Can be null
        this.amount = amount;
        this.currency = currency; // Allows overriding the default "USD"
        this.status = status;
        // transactionDate defaults to LocalDateTime.now()
    }

     // 3. Alternative constructor if userSubscription is not always required at creation
     public Transaction(User user, BigDecimal amount, String currency, String status) {
        this.user = user;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
     }


    // --- Getter Methods ---

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public UserSubscription getUserSubscription() {
        return userSubscription;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public String getPaymentMethodDetails() {
        return paymentMethodDetails;
    }

    public String getStatus() {
        return status;
    }

    public String getExternalTransactionId() {
        return externalTransactionId;
    }

    // --- Setter Methods ---

    // Typically, the ID setter is not used for database-generated IDs,
    // but included for completeness.
    public void setId(Long id) {
        this.id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setUserSubscription(UserSubscription userSubscription) {
        this.userSubscription = userSubscription;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    // Note: transactionDate often isn't updated after creation,
    // but providing the setter allows for flexibility.
    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public void setPaymentMethodDetails(String paymentMethodDetails) {
        this.paymentMethodDetails = paymentMethodDetails;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setExternalTransactionId(String externalTransactionId) {
        this.externalTransactionId = externalTransactionId;
    }

    // --- Other methods (like toString(), equals(), hashCode()) ---
    // Consider adding these for better debugging and comparison.
}