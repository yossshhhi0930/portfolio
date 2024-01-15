package com.example.portfolio.controller;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.example.portfolio.entity.User;
import com.example.portfolio.entity.UserInf;
import com.example.portfolio.entity.User.Authority;
import com.example.portfolio.entity.UserEmailChange;
import com.example.portfolio.form.UserEditForm;
import com.example.portfolio.form.UserForm;
import com.example.portfolio.repository.UserRepository;
import com.example.portfolio.repository.UserEmailChangeRepository;
import com.example.portfolio.service.UserMailSendService;

@Controller
public class UsersController {
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private UserRepository repository;

	@Autowired
	private UserEmailChangeRepository emailChangeRepository;

	@Autowired
	private UserMailSendService userService;

	@Value("${security.tokenLifeTimeSeconds}")
	int tokenLifeTimeSeconds;

	// ユーザー登録画面表示
	@GetMapping(path = "/users/new")
	public String newUser(Model model) {
		UserForm form = new UserForm();
		model.addAttribute("form", form);
		return "users/new";
	}

	// ユーザー仮登録
	@PostMapping(path = "/user")
	public String precreate(@Validated @ModelAttribute("form") UserForm form, BindingResult result, Model model,
			RedirectAttributes redirAttrs) throws IOException {
		String name = form.getName();
		String email = form.getEmail();
		String password = form.getPassword();
		String token = UUID.randomUUID().toString();
		// メールアドレスが既に登録されいる場合に返すエラーの追加
		if (repository.findByUsername(email) != null) {
			FieldError fieldError = new FieldError(result.getObjectName(), "email", "その E メールは既に登録されています。");
			result.addError(fieldError);
		}
		if (result.hasErrors()) {
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-danger");
			model.addAttribute("message", "ユーザーの登録に失敗しました。");
			return "users/new";
		}
		User entity = new User(email, name, passwordEncoder.encode(password), Authority.ROLE_USER, token, false);
		repository.saveAndFlush(entity);
		userService.sendVerifyMail(name, email, token);
		model.addAttribute("hasMessage", true);
		model.addAttribute("class", "alert-info");
		model.addAttribute("message", "ユーザーの仮登録が完了しました。");
		model.addAttribute("uniqueMessage", "ご登録いただいたメールアドレスに、ユーザー本登録案内メールをお送りいたしました。メール添付リンクにアクセスし、本登録の完了をお願いいたします。");
		return "pages/default";
	}

	// ユーザー本登録
	@GetMapping(path = "/verify/{token}")
	public String verify(@PathVariable String token, Model model) throws IOException {
		User entity = repository.findByToken(token);
		// ユーザーが既に有効になっているか、URLに含まれるtokenに誤りがあるか、URLの有効期限が切れている場合にエラーを返す
		if (entity == null || entity.isEnabled()) {
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-danger");
			model.addAttribute("message", "URLが無効です。");
			return "pages/index";
		}
		entity.setEnabled(true);
		repository.saveAndFlush(entity);
		model.addAttribute("hasMessage", true);
		model.addAttribute("class", "alert-info");
		model.addAttribute("message", "ユーザーの登録が完了しました。");
		return "sessions/new";
	}

	// ユーザー詳細表示
	@GetMapping(path = "/users/detail")
	public String showDetail(Principal principal, Model model) throws IOException {
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		User entity = repository.findByUserId(user.getUserId());
		model.addAttribute("user", entity);
		return "users/detail";
	}

	// ユーザー編集画面表示
	@GetMapping(path = "/users/edit")
	public String showEditPage(Principal principal, Model model) throws IOException {
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		User entity = repository.findByUserId(user.getUserId());
		UserEditForm form = getUserEditForm(entity);
		model.addAttribute("form", form);
		return "users/edit";
	}

	// ユーザー編集
	@PostMapping(path = "/users/edit-complete")
	public String edit(Principal principal, @Validated @ModelAttribute("form") UserEditForm form, BindingResult result,
			Model model, RedirectAttributes redirAttrs) throws IOException {
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		User entity = repository.findByUserId(user.getUserId());
		String name = form.getName();
		String email = form.getEmail();
		// メールアドレスが既に登録されいる場合に返すエラーの追加
		if (!email.equals(entity.getUsername()) && repository.findByUsername(email) != null) {
			FieldError fieldError = new FieldError(result.getObjectName(), "email", "そのメールアドレスは既に登録されています。");
			result.addError(fieldError);
		}
		if (result.hasErrors()) {
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-danger");
			model.addAttribute("message", "ユーザーの編集に失敗しました。");
			return "users/edit";
		}
		// メールアドレスに変更がある場合の処理
		if (!email.equals(entity.getUsername())) {
			entity.setName(name);
			repository.saveAndFlush(entity);
			// 新しいメールアドレス一時保存用にUserEmailChangeエンティティを生成
			String token = UUID.randomUUID().toString();
			LocalDateTime expiryDate = LocalDateTime.now().plusSeconds(tokenLifeTimeSeconds);
			UserEmailChange emailChangeEntity = new UserEmailChange(entity.getUserId(), token, expiryDate, email);
			emailChangeRepository.saveAndFlush(emailChangeEntity);
			userService.sendEmailVerifMail(name, email, token);
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-info");
			model.addAttribute("message", "ユーザー名の変更及び、メールアドレス変更仮手続きが完了しました。");
			model.addAttribute("uniqueMessage",
					"ご登録いただいた新しいメールアドレスに、メールアドレス変更本手続き案内メールをお送りいたしました。メール添付リンクにアクセスし、手続きの完了をお願いいたします。");
			return "pages/default";
		}
		// メールアドレスに変更がない場合の処理
		entity.setName(name);
		repository.saveAndFlush(entity);
		redirAttrs.addFlashAttribute("hasMessage", true);
		redirAttrs.addFlashAttribute("class", "alert-info");
		redirAttrs.addFlashAttribute("message", "ユーザーの編集が完了しました。");
		return "redirect:/users/detail";
	}

	// メールアドレス変更本登録用パスワード確認画面表示
	@GetMapping(path = "/email-verify/{token}")
	public String emailVerify(@PathVariable String token, Model model, RedirectAttributes redirAttrs)
			throws IOException {
		UserEmailChange userEmailChange = emailChangeRepository.findByToken(token);
		// URLに含まれる、verificationCodeに誤りがある場合にエラーを返す
		if (userEmailChange == null) {
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-danger");
			model.addAttribute("message", "URLが無効です。");
			return "pages/index";
		}
		// URLの有効期限が切れている場合にエラーを返す
		if (LocalDateTime.now().isAfter(userEmailChange.getExpiryDate())) {
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-danger");
			model.addAttribute("message", "URLの有効期限が切れています。");
			return "pages/index";
		}
		User entity = repository.findByUserId(userEmailChange.getUserId());
		entity.setUsername(userEmailChange.getNewEmail());
		repository.saveAndFlush(entity);
		emailChangeRepository.delete(userEmailChange);
		redirAttrs.addFlashAttribute("hasMessage", true);
		redirAttrs.addFlashAttribute("class", "alert-info");
		redirAttrs.addFlashAttribute("message", "メールアドレス変更手続きが完了しました。");
		return "redirect:/users/detail";
	}

	// ユーザー削除
	@GetMapping(path = "/users/delete")
	public String delete(Principal principal, RedirectAttributes redirAttrs, Model model) throws IOException {
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		User entity = repository.findByUserId(user.getUserId());
		repository.delete(entity);
		model.addAttribute("hasMessage", true);
		model.addAttribute("class", "alert-info");
		model.addAttribute("message", "ユーザーの削除が完了しました。");
		return "pages/index";
	}

	// UserエンティティからUserEditFormエンティティの取得
	public UserEditForm getUserEditForm(User user) throws IOException {
		UserEditForm form = new UserEditForm();
		form.setName(user.getName());
		form.setEmail(user.getUsername());
		return form;
	}
}
