package com.example.portfolio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.portfolio.entity.PasswordReissueInfo;

@Repository
public interface PasswordReissueInfoRepository extends JpaRepository<PasswordReissueInfo, String> {
	PasswordReissueInfo findByToken(String token);

	PasswordReissueInfo findBySecret(String secret);
}
