package com.example.portfolio.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.portfolio.entity.PasswordReissueInfo;
import com.example.portfolio.entity.User;
import com.example.portfolio.repository.PasswordReissueInfoRepository;
import com.example.portfolio.repository.UserRepository;

import net.bytebuddy.utility.RandomString;

@Service
public class PasswordReissueServiceImpl implements PasswordReissueService {

 @Autowired
 PasswordReissueInfoRepository passwordReissueInfoRepository;

 @Autowired
 PasswordReissueMailSharedService mailSharedService;


 @Autowired
 PasswordEncoder passwordEncoder;

// @Autowired
// PasswordGenerator passwordGenerator; // (1)

 
 @Autowired
 private UserRepository repository;
 
// @Autowired
// UriComponentsBuilder uriComponentBuilder;
 
 @Autowired
 PasswordReissueFailureSharedService passwordReissueFailureSharedService;
 
 //@Resource(name = "passwordGenerationRules")
 //List<CharacterRule> passwordGenerationRules; //(2)

 @Value("${security.tokenLifeTimeSeconds}")
 int tokenLifeTimeSeconds; // (3)
 
 @Value("${security.tokenValidityThreshold}")
 int tokenValidityThreshold; // (1)


 @Value("${app.applicationBaseUrl}") // (1)
 String baseUrl;
 
 
 // omitted

 @Override
 public String createAndSendReissueInfo(String username) {
	 //秘密情報として用いるために、Passayのパスワード生成機能を用いて、パスワード生成規則に従った、長さ10のランダムな文字列を生成する。
     String rowSecret = RandomString.make(10);; // (4)
     
     //パスワード再発行用の認証情報に含まれるユーザ名のアカウント情報を取得する。
     User user = repository.findByUsername(username);
     
     //引数として渡されてきたユーザ名のアカウントが存在するかどうか確認する。存在しなかった場合、ユーザが存在しないことを知られないためにダミーの秘密情報を返す。
     if(user == null){ // (5)
         return rowSecret;
     }
     //トークンとして用いるために、java.util.UUID クラスのrandomUUID メソッドを用いてランダムな文字列を生成する。
     String token = UUID.randomUUID().toString(); // (7)
     
     //現在時刻に(3)の値を加えることにより、パスワード再発行用の認証情報の有効期限を計算する。
     LocalDateTime expiryDate =  LocalDateTime.now().plusSeconds(tokenLifeTimeSeconds); // (8)
    
     //パスワード再発行用の認証情報を作成し、ユーザ名、トークン、秘密情報、有効期限を設定する。
     PasswordReissueInfo info = new PasswordReissueInfo(); // (9)
     info.setUsername(username);
     info.setToken(token);
     info.setSecret(rowSecret); // (10)
     info.setExpiryDate(expiryDate);
     //パスワード再発行用の認証情報をデータベースに登録する。
     passwordReissueInfoRepository.saveAndFlush(info); // (11)
     
     // Send E-Mail
     UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("http://localhost:8080/");
     uriBuilder.pathSegment("reissue").pathSegment("resetpassword")
             .queryParam("form").queryParam("token", info.getToken());  // (2)
     String passwordResetUrl = uriBuilder.build().encode().toUriString();

     mailSharedService.sendReissueMail(user.getUsername(), passwordResetUrl); // (3)
   
     //生成した秘密情報を返す.
     return rowSecret;
 }
}