package com.snee.transactio.db.repo;

import com.snee.transactio.db.entities.user.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersAccountRepo extends JpaRepository<UserAccount, Integer> {
}
