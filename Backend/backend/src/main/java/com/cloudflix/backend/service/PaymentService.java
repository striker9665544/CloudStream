package com.cloudflix.backend.service; // Create this package if it doesn't exist

import com.cloudflix.backend.dto.request.TestPaymentRequest;
import com.cloudflix.backend.entity.*; // User, SubscriptionPlan, UserSubscription, Transaction
import com.cloudflix.backend.repository.*; // All 4 repositories
import com.cloudflix.backend.security.services.UserDetailsImpl; // To get current user
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SubscriptionPlanRepository planRepository;
    @Autowired
    private UserSubscriptionRepository userSubscriptionRepository;
    @Autowired
    private TransactionRepository transactionRepository;

    @Transactional // Important for multiple DB operations
    public boolean processTestPayment(TestPaymentRequest paymentRequest) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        SubscriptionPlan plan = planRepository.findById(paymentRequest.getPlanId())
                .orElseThrow(() -> new RuntimeException("Subscription plan not found"));

        // Simulate payment success/failure
        boolean paymentSuccessful = !paymentRequest.getCardNumber().endsWith("0"); // Ends in 0 fails

        Transaction transaction = new Transaction();
        transaction.setUser(currentUser);
        transaction.setAmount(plan.getPrice());
        transaction.setCurrency(plan.getCurrency());
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setPaymentMethodDetails("Test Card ****" + paymentRequest.getCardNumber().substring(12));
        transaction.setExternalTransactionId("TEST_" + UUID.randomUUID().toString());

        if (paymentSuccessful) {
            transaction.setStatus("SUCCESS");

            // Create or update subscription
            UserSubscription subscription = new UserSubscription();
            subscription.setUser(currentUser);
            subscription.setPlan(plan);
            subscription.setStartDate(LocalDate.now());
            subscription.setEndDate(LocalDate.now().plusDays(plan.getDurationDays()));
            subscription.setStatus("ACTIVE");
            subscription.setAutoRenew(true); // Default
            UserSubscription savedSubscription = userSubscriptionRepository.save(subscription);

            transaction.setUserSubscription(savedSubscription); // Link transaction to subscription
            transactionRepository.save(transaction);
            return true;
        } else {
            transaction.setStatus("FAILED");
            transactionRepository.save(transaction);
            return false;
        }
    }
}