package com.example.portfolio.service;

public interface PasswordReissueMailSharedService {

	void sendReissueMail(String username, String passwordResetUrl);

}
