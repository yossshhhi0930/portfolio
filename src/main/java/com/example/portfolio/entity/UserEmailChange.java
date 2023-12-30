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

@Entity
@Table(name = "user_email_changes")
@Data
public class UserEmailChange extends AbstractEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	public UserEmailChange() {
		super();
	}

	public UserEmailChange(Long userId, String token, LocalDateTime expiryDate, String newEmail) {
		this.userId = userId;
		this.token = token;
		this.expiryDate = expiryDate;
		this.newEmail = newEmail;
	}

	@Id
	@SequenceGenerator(name = "user_email_change_id_seq")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long Id;

	@Column(nullable = false)
	private Long userId;

	// ユーザーメールアドレス変更用URLに含めるために生成される文字列（トークン）
	@Column(nullable = false)
	private String token;

	// ユーザーメールアドレス変更のためのURLの有効期限
	private LocalDateTime expiryDate;

	@Column(nullable = false)
	private String newEmail;

}
