package com.snee.transactio.db.repo;

import com.snee.transactio.db.entities.user.UserPassword;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersPasswordRepo extends JpaRepository<UserPassword, Integer> {
}
