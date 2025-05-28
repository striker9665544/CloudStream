//src/main/java/com/cloudflix/backend/controller/PaymentController.java
package com.cloudflix.backend.controller;

import com.cloudflix.backend.dto.request.TestPaymentRequest;
import com.cloudflix.backend.dto.response.MessageResponse;
import com.cloudflix.backend.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping(value = "/test-transaction", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')") // Only logged-in users can make payments
    public ResponseEntity<?> makeTestPayment(@Valid @RequestBody TestPaymentRequest paymentRequest) {
        try {
            boolean success = paymentService.processTestPayment(paymentRequest);
            if (success) {
                return ResponseEntity.ok(new MessageResponse("Payment successful and subscription activated!"));
            } else {
                return ResponseEntity.badRequest().body(new MessageResponse("Payment failed. Please try again."));
            }
        } catch (RuntimeException e) {
            // Catch specific exceptions like UserNotFound, PlanNotFound if you prefer
            return ResponseEntity.badRequest().body(new MessageResponse("Error processing payment: " + e.getMessage()));
        }
    }

    // You might also want an endpoint to list available subscription plans
    // @GetMapping("/plans")
    // public ResponseEntity<?> getSubscriptionPlans() { ... }
}