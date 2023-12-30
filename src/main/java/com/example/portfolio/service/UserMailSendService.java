package com.example.portfolio.service;
import org.springframework.stereotype.Service;

@Service
public interface UserMailSendService {

    void sendVerifyMail(String name, String customerEmail,String verificationCode) ;

    void sendEmailVerifMail(String name, String customerEmail, String verificationCode) ;

}

    
