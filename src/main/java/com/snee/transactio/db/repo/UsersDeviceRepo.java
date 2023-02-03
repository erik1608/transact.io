package com.snee.transactio.db.repo;

import com.snee.transactio.db.entities.user.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersDeviceRepo extends JpaRepository<UserDevice, Integer> {
}
