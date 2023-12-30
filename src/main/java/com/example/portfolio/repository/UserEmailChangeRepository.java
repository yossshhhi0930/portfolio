package com.example.portfolio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.portfolio.entity.UserEmailChange;

@Repository
public interface UserEmailChangeRepository extends JpaRepository<UserEmailChange, String> {
	UserEmailChange findByToken(String token);
}
