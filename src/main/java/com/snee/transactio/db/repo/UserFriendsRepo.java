package com.snee.transactio.db.repo;

import com.snee.transactio.db.entities.user.User;
import com.snee.transactio.db.entities.user.UserRelationMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserFriendsRepo extends JpaRepository<UserRelationMapping, Integer> {
    void deleteById(Integer id);

    Optional<UserRelationMapping> findByFriendAndStatus(User friend, String status);
}
