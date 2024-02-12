package com.example.portfolio.service;

import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * パスワード再発行メール送信サービスの実装クラス
 */
@Service
public class PasswordReissueMailSendServiceImpl implements PasswordReissueMailSendService {

	// メール送信に使用するJavaMailSenderの注入
	@Autowired
	private JavaMailSender javaMailSender;

	// メールの送信元メールアドレス
	@Value("${spring.mail.username}")
	private String fromEmail;

	/**
	 * パスワード再設定メールの送信
	 *
	 * @param name             ユーザーの名前
	 * @param customerEmail    ユーザーのメールアドレス
	 * @param passwordResetUrl パスワード再設定URL
	 */
	public void sendReissueMail(String name, String customerEmail, String passwordResetUrl) {
		MimeMessage message = javaMailSender.createMimeMessage();
		MimeMessageHelper helper = null;
		try {
			// メールヘルパーの初期化
			helper = new MimeMessageHelper(message, true);
			// 送信元メールアドレスの設定
			helper.setFrom(fromEmail);
			// 送信先メールアドレスの設定
			helper.setTo(customerEmail);
			// メールの件名の設定
			helper.setSubject("【さいばい手帖】パスワード再設定のご案内");
			// メール本文のHTML形式の構築
			String insertMessage = "<html>" + "<head></head>" + "<body>" + "<h3>" + name + "様</h3>"
					+ "<div>さいばい手帖サイトより、パスワード再設定のご依頼を承りました。下記のリンクをクリックしてパスワードの再設定を行ってください。</div>" + "<a href ="
					+ passwordResetUrl + ">パスワードの再設定</a>" + "</body>" + "</html>";
			// メール本文の設定（HTML形式）
			helper.setText(insertMessage, true);
			// メールの送信
			javaMailSender.send(message);
		} catch (Exception e) {
			// 例外発生時のエラーハンドリング
			e.printStackTrace();
		}
	}
}
