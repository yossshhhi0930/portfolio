package com.example.portfolio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.portfolio.entity.UserEmailChange;

/**
 * メールアドレス変更リポジトリクラス
 */
@Repository
public interface UserEmailChangeRepository extends JpaRepository<UserEmailChange, Long> {
	/**
	 * トークンからメールアドレス変更エンティティを取得
	 *
	 * @param token トークン
	 * @return 該当するメールアドレス変更エンティティ
	 */
	UserEmailChange findByToken(String token);

	/**
	 * ユーザーIDから、ユーザーに対応する全てのメールアドレス変更エンティティを削除
	 *
	 * @param userId ユーザーID
	 */
	void deleteAllByUserId(Long userId);
}
