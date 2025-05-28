//src/main/java/com/cloudflix/backend/repository/SubscriptionPlanRepository.java
package com.cloudflix.backend.repository;

import com.cloudflix.backend.entity.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {}