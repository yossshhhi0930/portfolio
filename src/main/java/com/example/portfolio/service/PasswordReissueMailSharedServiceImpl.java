package com.example.portfolio.service;

import javax.mail.internet.MimeMessage;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

//omitted

@Service
@Transactional
public class PasswordReissueMailSharedServiceImpl implements PasswordReissueMailSharedService {
//
// @Autowired
// JavaMailSender mailSender; // (1)
//
// @Autowired
// @Named("passwordReissueMessage")
// SimpleMailMessage templateMessage; // (2)
//
// // omitted
//
// @Override
// public void send(String to, String text) {
//     SimpleMailMessage message = new SimpleMailMessage(templateMessage); // (3)
//     message.setTo(to);
//     message.setText(text);
//     mailSender.send(message);
// }
// 
 @Autowired
 private JavaMailSender javaMailSender;

	@Autowired
 ResourceLoader resourceLoader;

	@Value("${spring.mail.username}")
 private String fromEmail;

 public void sendReissueMail(String customerEmail,String passwordResetUrl) {

     MimeMessage message = javaMailSender.createMimeMessage();
     MimeMessageHelper helper = null;


     // リンク先のページはフロントエンド(Next.js)側で実装する(dynamicRouting使う)
     // リンク先に飛んだときにverifyApiを実行する.
     // apiを実行する際にRequestBodyにverifyCodeを詰める

     try {
         helper = new MimeMessageHelper(message, true);
         helper.setFrom("yossshhhi@gmail.com");
         helper.setTo(customerEmail);
         helper.setSubject("Reissue Password");
         String insertMessage = "<html>"
                 + "<head></head>"
                 + "<body>"
                 + "<h3>Hello " + customerEmail + "</h3>"
                 +"<div>下記のリンクよりパスワードの再設定を行って下さい。</div>"
                 +"<a href ="+passwordResetUrl+">パスワードの再設定</a>"
                 + "</body>"
                 + "</html>";

         helper.setText(insertMessage, true);
         javaMailSender.send(message);

     } catch (Exception e) {
         e.printStackTrace();
         
     }

     
 }
}



