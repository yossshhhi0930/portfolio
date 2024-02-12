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
import com.example.portfolio.repository.UserRepository;

/**
 * ユーザーサービスの実装クラス
 */
@Service
public class UserServiceImpl implements UserService {

	// ユーザーリポジトリの注入
	@Autowired
	UserRepository repository;

	// ユーザーエンティティの作成後、無効のままである場合に削除されるまでの時間
	@Value("${security.tokenLifeTimeSeconds}")
	int tokenLifeTimeSeconds;

	/**
	 * 作成後、無効のまま一定時間経過したユーザーエンティティの削除
	 */
	@Scheduled(cron = "*/10 * * * * *") // 毎時実行
	@Transactional
	public void cleanupEntities() {
		// 現在時刻から指定された時間前の日時を生成
		LocalDateTime cutoffTime = LocalDateTime.now().minus(tokenLifeTimeSeconds, ChronoUnit.SECONDS);
		// LocalDateTimeをDate型に変換
		Date cutoffDate = Date.from(cutoffTime.atZone(ZoneId.systemDefault()).toInstant());
		// 一定時間経過した無効のユーザーエンティティを削除
		repository.deleteByEnabledFalseAndCreatedAtBefore(cutoffDate);
	}
}
