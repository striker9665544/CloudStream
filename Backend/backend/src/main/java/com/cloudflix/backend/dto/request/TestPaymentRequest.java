package com.cloudflix.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class TestPaymentRequest {
    @NotNull
    private Long planId; // ID of the SubscriptionPlan

    // Dummy card details
    @NotBlank
    @Size(min = 16, max = 16, message = "Card number must be 16 digits")
    private String cardNumber;

    @NotBlank
    private String expiryMonth; // MM

    @NotBlank
    private String expiryYear;  // YYYY

    @NotBlank
    @Size(min = 3, max = 4)
    private String cvv;

    // Or for UPI
    private String upiId;

    // If payment not tied to a plan, maybe an amount is passed directly
    // private BigDecimal amount;
    // private String currency;
    
 // --- Constructors ---

    // 1. No-Argument Constructor
    // Required for Spring MVC/Spring Boot to bind the request body (JSON/Form data)
    // to this DTO.
    public TestPaymentRequest() {
    }

    // 2. Constructor with card details
    // Useful for creating a request object specifically with card info
    public TestPaymentRequest(Long planId, String cardNumber, String expiryMonth, String expiryYear, String cvv) {
        this.planId = planId;
        this.cardNumber = cardNumber;
        this.expiryMonth = expiryMonth;
        this.expiryYear = expiryYear;
        this.cvv = cvv;
        // upiId remains null
    }

    // 3. Constructor with UPI details
    // Useful for creating a request object specifically with UPI info
    public TestPaymentRequest(Long planId, String upiId) {
        this.planId = planId;
        this.upiId = upiId;
        // Card fields remain null/blank
    }

    // 4. Constructor with all possible fields (allows setting any combination, incl. nulls)
     public TestPaymentRequest(Long planId, String cardNumber, String expiryMonth, String expiryYear, String cvv, String upiId) {
         this.planId = planId;
         this.cardNumber = cardNumber;
         this.expiryMonth = expiryMonth;
         this.expiryYear = expiryYear;
         this.cvv = cvv;
         this.upiId = upiId;
     }


    // --- Getter Methods ---

    public Long getPlanId() {
        return planId;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getExpiryMonth() {
        return expiryMonth;
    }

    public String getExpiryYear() {
        return expiryYear;
    }

    public String getCvv() {
        return cvv;
    }

    public String getUpiId() {
        return upiId;
    }

    // --- Setter Methods ---
    // Setters are needed for request body binding

    public void setPlanId(Long planId) {
        this.planId = planId;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public void setExpiryMonth(String expiryMonth) {
        this.expiryMonth = expiryMonth;
    }

    public void setExpiryYear(String expiryYear) {
        this.expiryYear = expiryYear;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public void setUpiId(String upiId) {
        this.upiId = upiId;
    }

    // --- Optional: Validation Helper ---
    // You might add a method to check if either card details or UPI is provided
    // public boolean hasCardDetails() {
    //     return cardNumber != null && !cardNumber.trim().isEmpty();
    // }

    // public boolean hasUpiId() {
    //      return upiId != null && !upiId.trim().isEmpty();
    // }
}