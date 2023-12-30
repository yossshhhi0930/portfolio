package com.example.portfolio.service;

import javax.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class UserMailSendServiceImpl implements UserMailSendService {

	@Autowired
	private JavaMailSender javaMailSender;

	@Autowired
	ResourceLoader resourceLoader;

	@Value("${spring.mail.username}")
	private String fromEmail;

	@Value("${app.base-url}")
	private String baseUrl;

	// ユーザー本登録用URLのメール送信
	public void sendVerifyMail(String name, String customerEmail, String token) {
		MimeMessage message = javaMailSender.createMimeMessage();
		MimeMessageHelper helper = null;
		try {
			helper = new MimeMessageHelper(message, true);
			helper.setFrom(fromEmail);
			helper.setTo(customerEmail);
			helper.setSubject("【さいばい手帖】ユーザー本登録のご案内");
			String insertMessage = "<html>" + "<head></head>" + "<body>" + "<h3>" + name + "様</h3>"
					+ "<div>さいばい手帖のユーザー仮登録が完了しました。下リンクをクリックして有効化してください。</div>" + "<a href = " + baseUrl + "/verify/"
					+ token + ">ユーザの有効化</a>" + "</body>" + "</html>";
			helper.setText(insertMessage, true);
			javaMailSender.send(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// メールアドレス変更本手続き用URLのメール送信
	public void sendEmailVerifMail(String name, String customerEmail, String token) {
		MimeMessage message = javaMailSender.createMimeMessage();
		MimeMessageHelper helper = null;
		try {
			helper = new MimeMessageHelper(message, true);
			helper.setFrom("fromEmail");
			helper.setTo(customerEmail);
			helper.setSubject("【さいばい手帖】メールアドレス変更本手続きのご案内");
			String insertMessage = "<html>" + "<head></head>" + "<body>" + "<h3>" + name + "様</h3>"
					+ "<div>さいばい手帖サイトより、メールアドレス変更のご依頼を承りました。下リンクをクリックして有効化してください。</div>" + "<a href = " + baseUrl
					+ "/email-verify/" + token + ">新しいメールアドレスの有効化</a>" + "</body>" + "</html>";
			helper.setText(insertMessage, true);
			javaMailSender.send(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
