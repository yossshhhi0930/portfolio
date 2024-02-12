package com.example.portfolio.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Data;

/**
 * パスワード発行失敗エンティティクラス
 */
@Entity
@Table(name = "failed_password")
@Data
public class FailedPasswordReissue extends AbstractEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * デフォルトコンストラクタ
	 */
	public FailedPasswordReissue() {
		super();
	}

	/**
	 * コンストラクタ
	 *
	 * @param email メールアドレス
	 */
	public FailedPasswordReissue(String email) {
		this.email = email;
	}

	/** パスワード再発行失敗ID */
	@Id
	@SequenceGenerator(name = "failedPassword_id_seq")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long Id;

	/**
	 * メールアドレス<br>
	 * パスワードの再発行を失敗したユーザーの登録メールアドレス
	 */
	@Column(nullable = false)
	private String email;

}
