package com.example.portfolio.service;

import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.portfolio.entity.PasswordReissueInfo;
import com.example.portfolio.form.PasswordResetForm;

public interface PasswordReissueService {

	String createAndSendReissueInfo(String username);

}
