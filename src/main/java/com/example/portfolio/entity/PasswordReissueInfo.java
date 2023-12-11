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

import com.example.portfolio.entity.User.Authority;

import lombok.Data;

@Entity
@Table(name = "passwordReissueInfo")
@Data
public class PasswordReissueInfo extends AbstractEntity implements Serializable {
    private static final long serialVersionUID = 1L; 

	
	@Id
    @SequenceGenerator(name = "users_id_seq")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long Id;
	
	//パスワード再発行対象のユーザ名
	@Column(nullable = false)
	private String username; // (1)

    //パスワード再発行用URLに含めるために生成される文字列（トークン）
    private String token; // (2)

    //パスワード再発行時にユーザを確認するための文字列（秘密情報）
    private String secret; // (3)

    //パスワード再発行のための認証情報の有効期限
    private LocalDateTime expiryDate; // (4)

}