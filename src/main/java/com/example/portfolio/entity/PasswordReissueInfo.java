package com.example.portfolio.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Data;

/**
 * パスワード再発行エンティティクラス
 */
@Entity
@Table(name = "password_reissue")
@Data
public class PasswordReissueInfo extends AbstractEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * デフォルトコンストラクタ
	 */
	public PasswordReissueInfo() {
		super();
	}

	/**
	 * コンストラクタ
	 *
	 * @param username   メールアドレス
	 * @param token      トークン
	 * @param secret     秘密情報
	 * @param expiryDate 有効期限
	 */
	public PasswordReissueInfo(String username, String token, String secret, LocalDateTime expiryDate) {
		this.username = username;
		this.token = token;
		this.secret = secret;
		this.expiryDate = expiryDate;
	}

	/** パスワード再発行ID */
	@Id
	@SequenceGenerator(name = "passwordReissue_id_seq")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** メールアドレス */
	@Column(nullable = false)
	private String username;

	/**
	 * トークン<br>
	 * パスワード再発行URLに含め、認証に使用する文字列
	 */
	@Column(nullable = false)
	private String token;

	/**
	 * 秘密情報<br>
	 * パスワード再発行時のユーザー認証に使用する秘密情報
	 */
	@Column(nullable = false)
	private String secret;

	/**
	 * 有効期限<br>
	 * パスワード再発行URLの有効期限
	 */
	@Column(nullable = false)
	private LocalDateTime expiryDate;

}