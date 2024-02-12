package com.example.portfolio.service;

import javax.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * ユーザーに関するメール送信サービスの実装クラス
 */
@Service
public class UserMailSendServiceImpl implements UserMailSendService {

	// メール送信に使用するJavaMailSenderの注入
	@Autowired
	private JavaMailSender javaMailSender;

	// メールの送信元メールアドレス
	@Value("${spring.mail.username}")
	private String fromEmail;

	// URL生成の際に、基本となるURL
	@Value("${app.base-url}")
	private String baseUrl;

	/**
	 * ユーザー本登録メールの送信
	 *
	 * @param name          ユーザーの名前
	 * @param customerEmail ユーザーのメールアドレス
	 * @param token         トークン
	 */
	public void sendVerifyMail(String name, String customerEmail, String token) {
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
			helper.setSubject("【さいばい手帖】ユーザー本登録のご案内");
			// メール本文のHTML形式の構築
			String insertMessage = "<html>" + "<head></head>" + "<body>" + "<h3>" + name + "様</h3>"
					+ "<div>さいばい手帖のユーザー仮登録が完了しました。下リンクをクリックして有効化してください。</div>" + "<a href = " + baseUrl + "/verify/"
					+ token + ">ユーザの有効化</a>" + "</body>" + "</html>";
			// メール本文の設定（HTML形式）
			helper.setText(insertMessage, true);
			// メールの送信
			javaMailSender.send(message);
		} catch (Exception e) {
			// 例外発生時のエラーハンドリング
			e.printStackTrace();
		}
	}

	/**
	 * メールアドレス変更本手続メールの送信
	 *
	 * @param name          ユーザーの名前
	 * @param customerEmail ユーザーのメールアドレス
	 * @param token         トークン
	 */
	public void sendEmailVerifMail(String name, String customerEmail, String token) {
		MimeMessage message = javaMailSender.createMimeMessage();
		MimeMessageHelper helper = null;
		try {
			// メールヘルパーの初期化
			helper = new MimeMessageHelper(message, true);
			// 送信元メールアドレスの設定
			helper.setFrom("fromEmail");
			// 送信先メールアドレスの設定
			helper.setTo(customerEmail);
			// メールの件名の設定
			helper.setSubject("【さいばい手帖】メールアドレス変更本手続きのご案内");
			// メール本文のHTML形式の構築
			String insertMessage = "<html>" + "<head></head>" + "<body>" + "<h3>" + name + "様</h3>"
					+ "<div>さいばい手帖サイトより、メールアドレス変更のご依頼を承りました。下リンクをクリックして有効化してください。</div>" + "<a href = " + baseUrl
					+ "/email-verify/" + token + ">新しいメールアドレスの有効化</a>" + "</body>" + "</html>";
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
