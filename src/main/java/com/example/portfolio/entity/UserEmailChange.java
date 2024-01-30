package com.example.portfolio.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * メールアドレス変更エンティティクラス
 */
@Entity
@Table(name = "user_email_changes")
@Data
public class UserEmailChange extends AbstractEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * デフォルトコンストラクタ
	 */
	public UserEmailChange() {
		super();
	}

	/**
	 * コンストラクタ
	 *
	 * @param userId     ユーザーID
	 * @param token      トークン
	 * @param expiryDate 有効期限
	 * @param newEmail   新しいメールアドレス
	 */
	public UserEmailChange(Long userId, String token, LocalDateTime expiryDate, String newEmail) {
		this.userId = userId;
		this.token = token;
		this.expiryDate = expiryDate;
		this.newEmail = newEmail;
	}

	/** メールアドレス変更ID */
	@Id
	@SequenceGenerator(name = "user_email_change_id_seq")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long Id;

	/**
	 * ユーザーID<br>
	 * メールアドレスの再設定を行うユーザー
	 */
	@Column(nullable = false)
	private Long userId;

	/**
	 * トークン<br>
	 * ユーザーメールアドレス変更URLに含め、認証に使用する文字列
	 */
	@Column(nullable = false)
	private String token;

	/**
	 * 有効期限<br>
	 * ユーザーメールアドレス変更URLの有効期限
	 */
	private LocalDateTime expiryDate;

	/** 新しいメールアドレス */
	@Column(nullable = false)
	private String newEmail;

}
