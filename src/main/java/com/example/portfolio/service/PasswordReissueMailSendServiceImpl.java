package com.example.portfolio.service;

import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class PasswordReissueMailSendServiceImpl implements PasswordReissueMailSendService {

	@Autowired
	private JavaMailSender javaMailSender;

	@Autowired
	ResourceLoader resourceLoader;

	@Value("${spring.mail.username}")
	private String fromEmail;

	// パスワード再設定用URLのメール送信
	public void sendReissueMail(String name, String customerEmail, String passwordResetUrl) {
		MimeMessage message = javaMailSender.createMimeMessage();
		MimeMessageHelper helper = null;
		try {
			helper = new MimeMessageHelper(message, true);
			helper.setFrom(fromEmail);
			helper.setTo(customerEmail);
			helper.setSubject("【さいばい手帖】パスワード再設定のご案内");
			String insertMessage = "<html>" + "<head></head>" + "<body>" + "<h3>" + name + "様</h3>"
					+ "<div>さいばい手帖サイトより、パスワード再設定のご依頼を承りました。下記のリンクをクリックしてパスワードの再設定を行ってください。</div>" + "<a href ="
					+ passwordResetUrl + ">パスワードの再設定</a>" + "</body>" + "</html>";
			helper.setText(insertMessage, true);
			javaMailSender.send(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
