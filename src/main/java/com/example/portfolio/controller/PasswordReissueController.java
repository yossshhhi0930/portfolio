package com.example.portfolio.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.portfolio.entity.FailedPasswordReissue;
import com.example.portfolio.entity.PasswordReissueInfo;
import com.example.portfolio.entity.User;
import com.example.portfolio.form.CreateReissueInfoForm;
import com.example.portfolio.form.PasswordResetForm;
import com.example.portfolio.repository.PasswordReissueInfoRepository;
import com.example.portfolio.repository.UserRepository;
import com.example.portfolio.service.FailedPasswordReissueServiceImpl;
import com.example.portfolio.service.PasswordReissueMailSendService;
import com.example.portfolio.repository.FailedPasswordReissueRepository;

import net.bytebuddy.utility.RandomString;

@Controller
public class PasswordReissueController {

	@Autowired
	private UserRepository repository;

	@Autowired
	PasswordReissueInfoRepository passwordReissueInfoRepository;

	@Autowired
	FailedPasswordReissueRepository failedPasswordRepository;

	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	PasswordReissueMailSendService mailSendService;

	@Autowired
	FailedPasswordReissueServiceImpl failedPasswordReissueServiceImpl;

	@Value("${app.base-url}")
	private String baseUrl;

	@Value("${security.tokenValidityThreshold}")
	int tokenValidityThreshold;

	@Value("${security.tokenLifeTimeSeconds}")
	int tokenLifeTimeSeconds;

	// メールアドレスを入力する画面を表示
	@GetMapping(path = "/reissue")
	public String showCreateReissueInfoForm(Model model) {
		model.addAttribute("form", new CreateReissueInfoForm());
		return "passwordreissue/createReissueInfoForm";
	}

	// パスワード再設定用認証情報の生成、登録、メール送信
	@PostMapping(path = "/reissue/create")
	public String createReissueInfo(Principal principal, @Validated @ModelAttribute("form") CreateReissueInfoForm form,
			BindingResult result, Model model) {
		String email = form.getEmail();
		User user = repository.findByUsername(email);
		// メールアドレスの登録がない場合に返すエラーの追加
		if (repository.findByUsername(email) == null) {
			FieldError fieldError = new FieldError(result.getObjectName(), "email", "そのメールアドレスは登録されていません。");
			result.addError(fieldError);
		}
		if (result.hasErrors()) {
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-danger");
			model.addAttribute("message", "パスワード再設定用メール送信に失敗しました。");
			return "passwordreissue/createReissueInfoForm";
		}
		String secret = RandomString.make(10);
		String token = UUID.randomUUID().toString();
		LocalDateTime expiryDate = LocalDateTime.now().plusSeconds(tokenLifeTimeSeconds);
		PasswordReissueInfo info = new PasswordReissueInfo(user.getUsername(), token, secret, expiryDate);
		passwordReissueInfoRepository.saveAndFlush(info);
		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(baseUrl);
		uriBuilder.pathSegment("reissue").pathSegment("resetpassword").queryParam("token", info.getToken());
		String passwordResetUrl = uriBuilder.build().encode().toUriString();
		mailSendService.sendReissueMail(user.getName(), user.getUsername(), passwordResetUrl);
		model.addAttribute("hasMessage", true);
		model.addAttribute("class", "alert-info");
		model.addAttribute("message", "パスワード再設定用メールを送信しました。");
		model.addAttribute("uniqueMessage",
				"パスワード再設定案内メールをお送りいたしました。メール添付リンクにアクセスし、次の秘密情報を入力して手続きの完了をお願いいたします。秘密情報：" + secret);
		return "pages/default";
	}

	// パスワード再設定画面表示
	@GetMapping(path = "/reissue/resetpassword")
	public String showPasswordResetForm(@RequestParam("token") String token, Model model) {
		PasswordReissueInfo info = passwordReissueInfoRepository.findByToken(token);
		// URLのクエリーに誤りがある場合にエラーを返す
		if (info == null) {
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-danger");
			model.addAttribute("message", "URLが無効です。");
			return "pages/index";
		}
		// URLの有効期限が切れている場合にエラーを返す
		if (LocalDateTime.now().isAfter(info.getExpiryDate())) {
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-danger");
			model.addAttribute("message", "URLの有効期限が切れています。");
			return "pages/index";
		}
		String email = info.getUsername();
		// パスワード再設定の失敗が上限回数を超えている場合にエラーを返す
		int count = failedPasswordRepository.countByEmail(email);
		if (count >= tokenValidityThreshold) {
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-danger");
			model.addAttribute("message", "パスワード再設定の失敗が上限回数を超えています。一定時間経過した後、再度URLを発行してください。");
			return "pages/index";
		}
		PasswordResetForm form = new PasswordResetForm();
		form.setEmail(info.getUsername());
		form.setToken(token);
		model.addAttribute("form", form);
		return "passwordreissue/passwordResetForm";
	}

	// パスワードの再設定
	@PostMapping(path = "/reissue/resetpassword")
	public String resetPassword(@Validated @ModelAttribute("form") PasswordResetForm form, BindingResult result,
			Model model, RedirectAttributes attributes) {
		String secret = form.getSecret();
		String token = form.getToken();
		String email = form.getEmail();
		String password = form.getPassword();
		PasswordReissueInfo info = passwordReissueInfoRepository.findBySecret(secret);
		// パスワード再設定の失敗カウント用のFailedPasswordエンティティを生成
		FailedPasswordReissue event = new FailedPasswordReissue(email);
		failedPasswordRepository.saveAndFlush(event);
		// 秘密情報に誤りがある場合に返すエラーの追加
		if (info == null || !(token.equals(info.getToken()))) {
			FieldError fieldError = new FieldError(result.getObjectName(), "secret", "秘密情報に誤りがあります。");
			result.addError(fieldError);
		}
		// パスワード再設定の失敗が上限回数を超えている場合にエラーを返す
		int count = failedPasswordRepository.countByEmail(email);
		if (count >= tokenValidityThreshold) {
			FieldError fieldError = new FieldError(result.getObjectName(), "null",
					"パスワード再設定の失敗が上限回数を超えています。一定時間経過した後、再度URLを発行してください。");
			result.addError(fieldError);
		}
		if (result.hasErrors()) {
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-danger");
			model.addAttribute("message", "パスワード再設定に失敗しました。");
			return "passwordreissue/passwordResetForm";
		}
		passwordReissueInfoRepository.delete(info);
		User user = repository.findByUsername(email);
		user.setPassword(passwordEncoder.encode(password));
		repository.saveAndFlush(user);
		// 現在保持している認証情報をクリア
		SecurityContextHolder.clearContext();
		model.addAttribute("hasMessage", true);
		model.addAttribute("class", "alert-info");
		model.addAttribute("message", "パスワード再設定が完了しました。");
		return "sessions/new";
	}
}