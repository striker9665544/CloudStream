//src/main/java/com/cloudflix/backend/repository/TransactionRepository.java
package com.cloudflix.backend.repository;

import com.cloudflix.backend.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {}