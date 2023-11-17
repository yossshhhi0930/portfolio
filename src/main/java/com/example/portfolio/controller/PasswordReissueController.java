package com.example.portfolio.controller;

import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.portfolio.entity.FailedPasswordReissue;
import com.example.portfolio.entity.PasswordReissueInfo;
import com.example.portfolio.entity.User;
import com.example.portfolio.form.CreateReissueInfoForm;
import com.example.portfolio.form.PasswordResetForm;
import com.example.portfolio.form.UserForm;
import com.example.portfolio.repository.PasswordReissueInfoRepository;
import com.example.portfolio.repository.UserRepository;
import com.example.portfolio.service.PasswordReissueFailureSharedService;
import com.example.portfolio.service.PasswordReissueFailureSharedServiceImpl;
import com.example.portfolio.service.PasswordReissueService;
import com.example.portfolio.repository.FailedPasswordReissueRepository;
import com.example.portfolio.service.PasswordReissueServiceImpl;

@Controller
public class PasswordReissueController {

 @Autowired
 PasswordReissueService passwordReissueService;
 
 @Autowired
 private UserRepository repository;
 
 @Autowired
 PasswordReissueInfoRepository passwordReissueInfoRepository;
 
 @Autowired
 FailedPasswordReissueRepository failedPasswordReissueRepository;
 
 @Autowired
 PasswordEncoder passwordEncoder;
 
 @Autowired
 PasswordReissueFailureSharedService passwordReissueFailureSharedService;
 
 @Autowired
 PasswordReissueServiceImpl passwordReissueServiceImpl;
 
 @Value("${security.tokenValidityThreshold}")
 int tokenValidityThreshold;

 @GetMapping(path = "/reissue") //@RequestMapping(value = "create", params = "form")
 public String showCreateReissueInfoForm(Model model) {
	 model.addAttribute("form", new CreateReissueInfoForm());
	 return "passwordreissue/createReissueInfoForm";
 }

 @RequestMapping(value = "/reissue/create", method = RequestMethod.POST)//@RequestMapping(value = "create", method = RequestMethod.POST)
 public String createReissueInfo(@Validated @ModelAttribute("form")CreateReissueInfoForm form, BindingResult result, Model model, RedirectAttributes attributes) {
	 String email = form.getEmail();
	 if (repository.findByUsername(email) == null) {
         FieldError fieldError = new FieldError(result.getObjectName(), "email", "その E メールは登録されていません。");
         result.addError(fieldError);
     }
     if (result.hasErrors()) {
     	model.addAttribute("hasMessage", true);
     	model.addAttribute("class", "alert-danger");
     	model.addAttribute("message", "メール送信に失敗しました。");
     	return "passwordreissue/createReissueInfoForm";
     }
     String rawSecret = passwordReissueService.createAndSendReissueInfo(form.getEmail()); // (1)
     attributes.addFlashAttribute("secret", rawSecret);
     return "redirect:/reissue/create/complete";
 }
 @GetMapping(path ="/reissue/create/complete")
 public String createReissueInfoComplete(@ModelAttribute("secret") String secret, Model model) {
	 model.addAttribute("secret", secret);
	 return "passwordreissue/createReissueInfoComplete";
 }
 
 @GetMapping(path ="/reissue/resetpassword")
 public String showPasswordResetForm(PasswordResetForm form, Model model,@RequestParam("token") String token, RedirectAttributes attributes) { // (1)
	 PasswordReissueInfo info = passwordReissueInfoRepository.findByToken(token); // (1)

     if (info == null) {
    	 model.addAttribute("hasMessage", true);
	     model.addAttribute("class", "alert-danger");
	     model.addAttribute("message", "認証コードが正しくありません。");
	     	return "pages/index";
     }

     if (LocalDateTime.now().isAfter(info.getExpiryDate())) { // (2)
    	 model.addAttribute("hasMessage", true);
	     model.addAttribute("class", "alert-danger");
	     model.addAttribute("message", "URLの有効期限が切れています。");
	     	return "pages/index";
     }
	     	
     int count = failedPasswordReissueRepository // (2)
             .countByToken(token);
     if (count >= tokenValidityThreshold) { // (3)
    	 model.addAttribute("hasMessage", true);
	     model.addAttribute("class", "alert-danger");
	     model.addAttribute("message", "パスワード再設定の失敗上限回数を超えています。");
	     	return "pages/index";
     }
     
     form.setEmail(info.getUsername());
     form.setToken(token);
     form.setSecret(info.getSecret());
     model.addAttribute("form", form);
     return "passwordreissue/passwordResetForm";
 }
 
 @RequestMapping(value = "/reissue/resetpassword", method = RequestMethod.POST)
 public String resetPassword(@Validated @ModelAttribute("form") PasswordResetForm form,BindingResult result, Model model, RedirectAttributes attributes) {
	 if (result.hasErrors()) {
	     	model.addAttribute("hasMessage", true);
	     	model.addAttribute("class", "alert-danger");
	     	model.addAttribute("message", "パスワード再設定に失敗しました。");
	     	return "passwordreissue/passwordResetForm";
     }

        	 PasswordReissueInfo info = passwordReissueInfoRepository.findBySecret(form.getSecret()); // (1)
        	 FailedPasswordReissue event = new FailedPasswordReissue(); // (2)
             event.setToken(form.getToken());
             event.setAttemptDate(LocalDateTime.now());
             failedPasswordReissueRepository.saveAndFlush(event); 
        	 if(info == null) {
        		 model.addAttribute("hasMessage", true);
        	     model.addAttribute("class", "alert-danger");
        	     model.addAttribute("message", "認証情報に誤りがあります。");
        	     	return "pages/index";
        	 }
           if (!(form.getSecret().equals(info.getSecret()))) { // (2)
                 model.addAttribute("hasMessage", true);
                 model.addAttribute("class", "alert-danger");
                 model.addAttribute("message", "認証情報に誤りがあります。");
                 
    	     	return "pages/index";
             }
           failedPasswordReissueRepository.delete(event);
          passwordReissueInfoRepository.delete(info); // (3)
           
             User user = repository.findByUsername(form.getEmail());
             user.setPassword(passwordEncoder.encode(form.getPassword()));
             repository.saveAndFlush(user);
             
         return "redirect:/reissue/resetpassword/complete";}
 
 
 @GetMapping(path ="/reissue/resetpassword/complete")
 public String resetPasswordComplete() {
     return "passwordreissue/passwordResetComplete";
 }

}