package com.example.portfolio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.portfolio.entity.PasswordReissueInfo;

/**
 * パスワード再発行リポジトリクラス
 */
@Repository
public interface PasswordReissueInfoRepository extends JpaRepository<PasswordReissueInfo, Long> {
	/**
	 * トークンからパスワード再発行エンティティを取得
	 *
	 * @param token トークン
	 * @return 該当するパスワード再発行エンティティ
	 */
	PasswordReissueInfo findByToken(String token);

	/**
	 * 秘密情報からパスワード再発行エンティティを取得
	 *
	 * @param secret 秘密情報
	 * @return 該当するパスワード再発行エンティティ
	 */
	PasswordReissueInfo findBySecret(String secret);

}
