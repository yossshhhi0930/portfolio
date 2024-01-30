package com.example.portfolio.repository;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.portfolio.entity.User;

/**
 * ユーザーリポジトリクラス
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	/**
	 * ユーザーIDからユーザーエンティティを取得
	 *
	 * @param userId ユーザーID
	 * @return 該当するユーザーエンティティ
	 */
	User findByUserId(Long userId);

	/**
	 * ユーザーメールアドレスからユーザーエンティティを取得
	 *
	 * @param username メールアドレス
	 * @return 該当するユーザーエンティティ
	 */
	User findByUsername(String username);

	/**
	 * トークンからユーザーエンティティの取得
	 *
	 * @param token トークン
	 * @return 該当するユーザーエンティティ
	 */
	User findByToken(String token);

	/**
	 * 有効でないユーザーかつ指定した日付よりも前の作成日時のユーザーエンティティを削除
	 *
	 * @param cutoffDate 指定日付
	 */
	void deleteByEnabledFalseAndCreatedAtBefore(Date cutoffDate);
}
