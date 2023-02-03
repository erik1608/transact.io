package com.snee.transactio.db.repo;

import com.snee.transactio.db.entities.user.Biometrics;
import com.snee.transactio.db.entities.user.UserDevice;
import com.snee.transactio.db.entities.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BiometricsRepo extends JpaRepository<Biometrics, Integer> {
	Biometrics findByUserAndDevice(User user, UserDevice device);
	void deleteById(Integer id);
}
