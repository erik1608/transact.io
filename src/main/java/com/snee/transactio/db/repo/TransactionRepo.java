package com.snee.transactio.db.repo;

import com.snee.transactio.db.entities.transaction.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepo extends JpaRepository<Transaction, Integer> {
}
