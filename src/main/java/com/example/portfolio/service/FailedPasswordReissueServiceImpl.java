package com.example.portfolio.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.portfolio.repository.FailedPasswordReissueRepository;

@Service
public class FailedPasswordReissueServiceImpl implements FailedPasswordReissueService {

	@Autowired
	FailedPasswordReissueRepository repository;

	@Value("${security.tokenLifeTimeSeconds}")
	int tokenLifeTimeSeconds;
	// 一定時間経過したエンティティの削除
	@Scheduled(cron = "*/10 * * * * *") // 毎時実行
	@Transactional
	public void cleanupEntities() {
		LocalDateTime cutoffTime = LocalDateTime.now().minus(tokenLifeTimeSeconds, ChronoUnit.SECONDS);
		Date cutoffDate = Date.from(cutoffTime.atZone(ZoneId.systemDefault()).toInstant());
		repository.deleteByCreatedAtBefore(cutoffDate);
	}
}
