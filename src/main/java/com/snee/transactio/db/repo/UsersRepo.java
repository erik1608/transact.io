package com.snee.transactio.db.repo;

import com.snee.transactio.db.entities.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsersRepo extends JpaRepository<User, Integer> {
	Optional<User> findByUsername(String username);
	Optional<User> findByEmail(String email);
}
