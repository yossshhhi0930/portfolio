package com.example.portfolio.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.portfolio.entity.User;
import com.example.portfolio.entity.User.Authority;
import com.example.portfolio.form.UserForm;
import com.example.portfolio.repository.UserRepository;
import com.example.portfolio.service.UserService;

import net.bytebuddy.utility.RandomString;


@Controller
public class UsersController {
	@Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository repository;
    
    @Autowired
    private UserService userService;

    @GetMapping(path = "/users/new")
    public String newUser(Model model) {
        model.addAttribute("form", new UserForm());
        return "users/new";
    }
    //ユーザー仮登録
    @RequestMapping(value = "/user", method = RequestMethod.POST)
    public String precreate(@Validated @ModelAttribute("form") UserForm form, BindingResult result, Model model, RedirectAttributes redirAttrs) {
        String name = form.getName();
        String email = form.getEmail();
        String password = form.getPassword();
        String passwordConfirmation = form.getPasswordConfirmation();
        String randomCode = RandomString.make(64);
        

        if (repository.findByUsername(email) != null) {
            FieldError fieldError = new FieldError(result.getObjectName(), "email", "その E メールはすでに使用されています。");
            result.addError(fieldError);
        }
        if (result.hasErrors()) {
        	model.addAttribute("hasMessage", true);
        	model.addAttribute("class", "alert-danger");
        	model.addAttribute("message", "ユーザー登録に失敗しました。");
            return "users/new";
        }
//      public User(String email, String name, String password, Authority authority,String verificationCode, boolean enabled) {
//      this.username = email;
//      this.name = name;
//      this.password = password;
//      this.authority = authority;
//      this.verificationCode = verificationCode;
//      this.enabled = enabled;
//  }

        User entity = new User(email, name, passwordEncoder.encode(password), Authority.ROLE_USER, randomCode, false);
        repository.saveAndFlush(entity);
        ((UserService) userService).sendVerifyMail(email,randomCode);
        
        model.addAttribute("hasMessage", true);
        model.addAttribute("class", "alert-info");
        model.addAttribute("message", "ユーザー仮登録が完了しました。");
       
        return "pages/default";
        
    }
    
    //ユーザー本登録
    //リクエストパラメータで送られてきたverificationCodeがverifyメソッドにより認証が正になれば、enabledの値をtrueに変更し、ユーザーエンティティを保存。
    //負であれば、エラーメッセージを表示し、新しい登録画面を表示する。
    @GetMapping(path ="/verify/{verificationCode}")
    public String verify(@PathVariable String verificationCode, Model model, RedirectAttributes redirAttrs) {
        User user = repository.findByVerificationCode(verificationCode);
        if (user == null || user.isEnabled()) {
        	model.addAttribute("hasMessage", true);
        	model.addAttribute("class", "alert-danger");
        	model.addAttribute("message", "認証コードが正しくありません。");
            return "pages/index";
        }
       
        user.setEnabled(true);
        	repository.saveAndFlush(user);
        	model.addAttribute("hasMessage", true);
            model.addAttribute("class", "alert-info");
            model.addAttribute("message", "ユーザー登録が完了しました。");
            return "sessions/new";
        
    }
    
//    @GetMapping(path ="/fogotPassword")
//    public String checkEmail(Model model) {
//    	return "pages/checkEmail";
//    }
//    @RequestMapping(value = "/sendPasswordResetMail", method = RequestMethod.POST)
//    public String sendPasswordResetMail(@RequestParam("email") String email, Model model) {
//    	User user = repository.findByUsername(email);
//    	if (user == null) {
//        	model.addAttribute("hasMessage", true);
//        	model.addAttribute("class", "alert-danger");
//        	model.addAttribute("message", "このメールアドレスは未登録です。");
//            return "pages/index";
//        }
//        	String randomCode = RandomString.make(64);
//        	userService.sendPasswordResettingMail(email, randomCode);
//        	user.setVerificationCode(randomCode);
//        	repository.saveAndFlush(user);
//        	model.addAttribute("hasMessage", true);
//            model.addAttribute("class", "alert-info");
//            model.addAttribute("message", "パスワード再設定用URLをメールアドレスに送信しました。");
//            return "pages/default";
//        
//    }
//    @GetMapping(path ="/resetPassword/{verificationCode}")
//    public String resetPassword(@PathVariable String verificationCode, Model model, RedirectAttributes redirAttrs) {
//        User user = repository.findByVerificationCode(verificationCode);
//        if (user == null) {
//        	model.addAttribute("hasMessage", true);
//        	model.addAttribute("class", "alert-danger");
//        	model.addAttribute("message", "このURLは無効です。");
//            return "pages/index";
//        }else {
//        	return "users/resetpassword";
//        }
//    }
//    
//    @RequestMapping(value = "/resetPaswordComplete/{userId}", method = RequestMethod.POST)
//    public String resetPaswordComplete(@PathVariable Long userId, @RequestParam("password") String password, Model model) {
//    	User user = repository.findByUserId(userId);
//    	user.setPassword(passwordEncoder.encode(password));
//    	repository.saveAndFlush(user);
//        
//        model.addAttribute("hasMessage", true);
//        model.addAttribute("class", "alert-info");
//        model.addAttribute("message", "パスワードの再設定が完了しました。");
//       
//        return "sessions/new";
//    	
//    
//    }
}
