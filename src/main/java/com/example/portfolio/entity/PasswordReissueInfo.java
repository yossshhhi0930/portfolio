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

@Entity
@Table(name = "password_reissue")
@Data
public class PasswordReissueInfo extends AbstractEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	public PasswordReissueInfo() {
		super();
	}

	public PasswordReissueInfo(String username, String token, String secret, LocalDateTime expiryDate) {
		this.username = username;
		this.token = token;
		this.secret = secret;
		this.expiryDate = expiryDate;
	}

	@Id
	@SequenceGenerator(name = "passwordReissue_id_seq")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String username;

	// パスワード再発行用URLに含めるために生成される文字列（トークン）
	@Column(nullable = false)
	private String token;

	// パスワード再発行時にユーザを確認するための文字列（秘密情報）
	@Column(nullable = false)
	private String secret;

	// パスワード再発行のための認証情報の有効期限
	@Column(nullable = false)
	private LocalDateTime expiryDate;

}