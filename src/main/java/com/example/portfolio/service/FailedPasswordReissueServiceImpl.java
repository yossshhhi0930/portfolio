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

/**
 * パスワード再発行失敗サービスの実装クラス
 */
@Service
public class FailedPasswordReissueServiceImpl implements FailedPasswordReissueService {

	// パスワード再発行失敗リポジトリの注入
	@Autowired
	FailedPasswordReissueRepository repository;

	// パスワード再発行失敗エンティティが作成されてから削除されるまでの時間
	@Value("${security.tokenLifeTimeSeconds}")
	int tokenLifeTimeSeconds;

	/**
	 * 作成後、一定時間経過したパスワード再発行失敗エンティティの削除
	 */
	@Scheduled(cron = "*/10 * * * * *") // 毎時実行
	@Transactional
	public void cleanupEntities() {
		// 現在時刻から指定された時間前の日時を生成
		LocalDateTime cutoffTime = LocalDateTime.now().minus(tokenLifeTimeSeconds, ChronoUnit.SECONDS);
		// LocalDateTimeをDate型に変換
		Date cutoffDate = Date.from(cutoffTime.atZone(ZoneId.systemDefault()).toInstant());
		// 一定時間経過したパスワード再発行失敗エンティティを削除
		repository.deleteByCreatedAtBefore(cutoffDate);
	}
}
