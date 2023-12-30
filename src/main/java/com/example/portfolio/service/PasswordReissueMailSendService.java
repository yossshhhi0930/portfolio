package com.example.portfolio.service;

public interface PasswordReissueMailSendService {

	void sendReissueMail(String name, String username, String passwordResetUrl);

}
