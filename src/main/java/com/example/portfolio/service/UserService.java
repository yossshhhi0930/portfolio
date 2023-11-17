package com.example.portfolio.service;

import javax.mail.internet.MimeMessage;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class UserService {

	@Autowired
    private JavaMailSender javaMailSender;

	@Autowired
    ResourceLoader resourceLoader;

	@Value("${spring.mail.username}")
    private String fromEmail;

    public void sendVerifyMail(String customerEmail,String verificationCode) {

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = null;


        // リンク先のページはフロントエンド(Next.js)側で実装する(dynamicRouting使う)
        // リンク先に飛んだときにverifyApiを実行する.
        // apiを実行する際にRequestBodyにverifyCodeを詰める

        try {
            helper = new MimeMessageHelper(message, true);
            helper.setFrom("yossshhhi@gmail.com");
            helper.setTo(customerEmail);
            helper.setSubject("Ecshop Verification");
            String insertMessage = "<html>"
                    + "<head></head>"
                    + "<body>"
                    + "<h3>Hello " + customerEmail + "</h3>"
                    +"<div>ECショップのユーザー仮登録が完了しました。下リンクをクリックして有効化してください</div>"
                    +"<a href = http://localhost:8080/verify/"+verificationCode+">ユーザの有効化</a>"
                    + "</body>"
                    + "</html>";

            helper.setText(insertMessage, true);
            javaMailSender.send(message);

        } catch (Exception e) {
            e.printStackTrace();
        }
}
//    public boolean sendPasswordResettingMail(String customerEmail,String verificationCode) {
//
//        MimeMessage message = javaMailSender.createMimeMessage();
//        MimeMessageHelper helper = null;
//
//
//        // リンク先のページはフロントエンド(Next.js)側で実装する(dynamicRouting使う)
//        // リンク先に飛んだときにverifyApiを実行する.
//        // apiを実行する際にRequestBodyにverifyCodeを詰める
//
//        try {
//            helper = new MimeMessageHelper(message, true);
//            helper.setFrom("yossshhhi@gmail.com");
//            helper.setTo(customerEmail);
//            helper.setSubject("Reset password");
//            String insertMessage = "<html>"
//                    + "<head></head>"
//                    + "<body>"
//                    + "<h3>Hello " + customerEmail + "</h3>"
//                    +"<div>下記のリンクよりパスワードの再設定を行って下さい。</div>"
//                    +"<a href = http://localhost:8080/resetPassword/"+verificationCode+">パスワードの再設定</a>"
//                    + "</body>"
//                    + "</html>";
//
//            helper.setText(insertMessage, true);
//            javaMailSender.send(message);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//
//        return true;

//}
}

    
