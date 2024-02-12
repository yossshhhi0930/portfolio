package com.example.portfolio.repository;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.portfolio.entity.FailedPasswordReissue;

/**
 * パスワード再発行失敗リポジトリクラス
 */
@Repository
public interface FailedPasswordReissueRepository extends JpaRepository<FailedPasswordReissue, Long> {
	/**
	 * 指定したメールアドレスのパスワード再発行失敗エンティティの登録数（失敗回数）を取得
	 *
	 * @param email メールアドレス
	 * @return パスワード再発行失敗エンティティの登録数（失敗回数）
	 */
	int countByEmail(String email);

	/**
	 * 指定した日付よりも前の作成日時のパスワード再発行失敗エンティティを削除
	 *
	 * @param cutoffDate 指定日付
	 */
	void deleteByCreatedAtBefore(Date cutoffDate);
}
