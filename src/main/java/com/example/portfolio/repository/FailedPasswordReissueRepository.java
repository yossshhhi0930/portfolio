package com.example.portfolio.repository;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.portfolio.entity.FailedPasswordReissue;

@Repository
public interface FailedPasswordReissueRepository extends JpaRepository<FailedPasswordReissue, Long> {
	int countByEmail(String email);

	void deleteByCreatedAtBefore(Date cutoffDate);
}
