package com.example.portfolio.repository;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.portfolio.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
	User findByUserId(Long userId);

	User findByUsername(String username);

	User findByToken(String token);

	void deleteByEnabledFalseAndCreatedAtBefore(Date cutoffDate);
}
