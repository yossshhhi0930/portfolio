package com.example.portfolio.service;

import org.springframework.stereotype.Service;

/**
 * パスワード再発行メール送信サービスクラス
 */
@Service
public interface PasswordReissueMailSendService {

	void sendReissueMail(String name, String username, String passwordResetUrl);

}
