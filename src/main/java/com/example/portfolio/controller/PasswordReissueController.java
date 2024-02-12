package com.example.portfolio.controller;

import java.io.IOException;
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
import org.springframework.web.util.UriComponentsBuilder;

import com.example.portfolio.entity.FailedPasswordReissue;
import com.example.portfolio.entity.PasswordReissueInfo;
import com.example.portfolio.entity.User;
import com.example.portfolio.form.CreateReissueInfoForm;
import com.example.portfolio.form.PasswordResetForm;
import com.example.portfolio.repository.PasswordReissueInfoRepository;
import com.example.portfolio.repository.UserRepository;
import com.example.portfolio.service.FailedPasswordReissueService;
import com.example.portfolio.service.PasswordReissueMailSendService;
import com.example.portfolio.repository.FailedPasswordReissueRepository;

import net.bytebuddy.utility.RandomString;

/**
 * パスワード再発行に関する処理を担当するコントローラクラス
 */
@Controller
public class PasswordReissueController {

	// ユーザーリポジトリの注入
	@Autowired
	private UserRepository repository;

	// パスワード再発行リポジトリの注入
	@Autowired
	PasswordReissueInfoRepository passwordReissueInfoRepository;

	// パスワード再発行失敗リポジトリの注入
	@Autowired
	FailedPasswordReissueRepository failedPasswordRepository;

	// パスワードエンコーダーの注入
	@Autowired
	PasswordEncoder passwordEncoder;

	// パスワード再発行メール送信サービスの注入
	@Autowired
	PasswordReissueMailSendService mailSendService;

	// パスワード再発行失敗サービスの注入
	@Autowired
	FailedPasswordReissueService failedPasswordReissueService;

	// URL生成の際に、基本となるURL
	@Value("${app.base-url}")
	private String baseUrl;

	// パスワード再発行上限失敗回数
	@Value("${security.tokenValidityThreshold}")
	int tokenValidityThreshold;

	// パスワード再発行URLの有効期限の設定に使用する時間
	@Value("${security.tokenLifeTimeSeconds}")
	int tokenLifeTimeSeconds;

	/**
	 * パスワード再発行案内メール送付先で、かつユーザー情報に登録しているメールメールアドレスを入力する画面の表示
	 *
	 * @param model ビューで使用するモデル
	 * @return メールアドレス入力画面
	 */
	@GetMapping(path = "/reissue")
	public String showCreateReissueInfoForm(Model model) {
		// メールアドレス入力フォームを初期化
		CreateReissueInfoForm form = new CreateReissueInfoForm();
		// 初期化したメールアドレス入力フォームをモデルに設定
		model.addAttribute("form", form);
		// メールアドレス入力画面を返す
		return "passwordReissue/createReissueInfoForm";
	}

	/**
	 * パスワード再設定用認証情報の生成、登録、メール送信<br>
	 * メールアドレス入力画面から送信されたフォームデータの検証、パスワード再設定エンティティの生成、登録、メール送信を行う
	 * 
	 * @param principal 認証情報
	 * @param form      メールアドレス入力フォームデータ
	 * @param result    フォーム検証の結果
	 * @param model     ビューで使用するモデル
	 * @return 検証結果に基づく画面
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	@PostMapping(path = "/reissue/create")
	public String createReissueInfo(Principal principal, @Validated @ModelAttribute("form") CreateReissueInfoForm form,
			BindingResult result, Model model) throws IOException {
		// フォームデータを変数に代入
		String email = form.getEmail();
		// フォームデータのメールアドレスからユーザーを取得
		User user = repository.findByUsername(email);
		// メールアドレスの登録がない場合に返すエラーの追加
		if (repository.findByUsername(email) == null) {
			FieldError fieldError = new FieldError(result.getObjectName(), "email", "そのメールアドレスは登録されていません。");
			result.addError(fieldError);
		}
		// エラーがあった場合に、メールアドレス入力画面を返し、エラーメッセージを表示
		if (result.hasErrors()) {
			// アラートメッセージをモデルに設定
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-danger");
			model.addAttribute("message", "パスワード再設定用メール送信に失敗しました。");
			// メールアドレス入力画面を返す
			return "passwordReissue/createReissueInfoForm";
		}
		// エラーが無かった場合
		// 秘密情報を生成
		String secret = RandomString.make(10);
		// パスワード再設定URLに含めるトークンを生成
		String token = UUID.randomUUID().toString();
		// パスワード再設定URLの有効期限の生成
		LocalDateTime expiryDate = LocalDateTime.now().plusSeconds(tokenLifeTimeSeconds);
		// パスワード再設定エンティティの生成
		PasswordReissueInfo info = new PasswordReissueInfo(user.getUsername(), token, secret, expiryDate);
		// パスワード再設定エンティティの保存
		passwordReissueInfoRepository.saveAndFlush(info);
		// パスワード再設定URLを生成
		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(baseUrl);
		uriBuilder.pathSegment("reissue").pathSegment("resetpassword").queryParam("token", info.getToken());
		String passwordResetUrl = uriBuilder.build().encode().toUriString();
		// パスワード再設定案内メールを送信
		mailSendService.sendReissueMail(user.getName(), user.getUsername(), passwordResetUrl);
		// アラートメッセージをモデルに設定
		model.addAttribute("hasMessage", true);
		model.addAttribute("class", "alert-info");
		model.addAttribute("message", "パスワード再設定用メールを送信しました。");
		// モデルにパスワード再設定の案内メッセージを設定
		model.addAttribute("uniqueMessage",
				"パスワード再設定案内メールをお送りいたしました。メール添付リンクにアクセスし、次の秘密情報を入力して手続きの完了をお願いいたします。秘密情報：" + secret);
		// パスワード再設定の案内画面を返す
		return "pages/default";
	}

	/**
	 * パスワード再設定画面の表示<br>
	 * パスワード再設定案内メールに添付したURLがクリックされた際に、URLの検証、再設定画面の表示を行う
	 *
	 * @param token トークン（URLのクエリーパラメータ）
	 * @param model ビューで使用するモデル
	 * @return 検証結果に基づく画面
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	@GetMapping(path = "/reissue/resetpassword")
	public String showPasswordResetForm(@RequestParam("token") String token, Model model) throws IOException {
		// トークンからパスワード再設定エンティティを取得
		PasswordReissueInfo info = passwordReissueInfoRepository.findByToken(token);
		// URLのクエリーに誤りがある場合にエラーを返す
		if (info == null) {
			// アラートメッセージをモデルに設定
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-danger");
			model.addAttribute("message", "URLが無効です。");
			// トップ画面を返す
			return "pages/index";
		}
		// URLの有効期限が切れている場合にエラーを返す
		if (LocalDateTime.now().isAfter(info.getExpiryDate())) {
			// アラートメッセージをモデルに設定
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-danger");
			model.addAttribute("message", "URLの有効期限が切れています。");
			// トップ画面を返す
			return "pages/index";
		}
		// 取得したパスワード再設定エンティティからメールアドレスを取得
		String email = info.getUsername();
		// 取得したメールアドレスからパスワード再設定の失敗回数を取得
		int count = failedPasswordRepository.countByEmail(email);
		// パスワード失敗回数が上限を超えている場合にエラーを返す
		if (count >= tokenValidityThreshold) {
			// アラートメッセージをモデルに設定
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-danger");
			model.addAttribute("message", "パスワード再設定の失敗が上限回数を超えています。一定時間経過した後、再度URLを発行してください。");
			// トップ画面を返す
			return "pages/index";
		}
		// エラーが無かった場合の処理
		// パスワード再設定フォームを初期化
		PasswordResetForm form = new PasswordResetForm();
		// 初期化したパスワード再設定フォームに、パスワード再設定エンティティのメールアドレスと、トークンを設定
		form.setEmail(info.getUsername());
		form.setToken(token);
		// パスワード再設定フォームをモデルに設定し、パスワード再設定画面を返す
		model.addAttribute("form", form);
		return "passwordReissue/passwordResetForm";
	}

	/**
	 * パスワードの再設定<br>
	 * パスワード再設定画面から送信されたフォームデータの検証、パスワードの更新を行う
	 * 
	 * @param form   パスワード再設定フォームデータ
	 * @param result フォーム検証の結果
	 * @param model  ビューで使用するモデル
	 * @return 検証結果に基づく画面
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	@PostMapping(path = "/reissue/resetpassword")
	public String resetPassword(@Validated @ModelAttribute("form") PasswordResetForm form, BindingResult result,
			Model model) throws IOException {
		// フォームデータを変数に代入
		String secret = form.getSecret();
		String token = form.getToken();
		String email = form.getEmail();
		String password = form.getPassword();
		// フォームデータの秘密情報からパスワード再設定エンティティを取得
		PasswordReissueInfo info = passwordReissueInfoRepository.findBySecret(secret);
		// フォームデータのメールアドレスから、パスワード再設定の失敗カウント用のパスワード再発行失敗エンティティを生成
		FailedPasswordReissue event = new FailedPasswordReissue(email);
		// パスワード再発行失敗エンティティを保存
		failedPasswordRepository.saveAndFlush(event);
		// 秘密情報に誤りがある場合に返すエラーの追加
		if (info == null || !(token.equals(info.getToken()))) {
			FieldError fieldError = new FieldError(result.getObjectName(), "secret", "秘密情報に誤りがあります。");
			result.addError(fieldError);
		}
		// パスワード再設定の失敗回数を取得
		int count = failedPasswordRepository.countByEmail(email);
		// パスワード失敗回数が上限を超えている場合にエラーを返す
		if (count >= tokenValidityThreshold) {
			FieldError fieldError = new FieldError(result.getObjectName(), "null",
					"パスワード再設定の失敗が上限回数を超えています。一定時間経過した後、再度URLを発行してください。");
			result.addError(fieldError);
		}
		// エラーがあった場合に、パスワード再設定画面を返し、エラーメッセージを表示
		if (result.hasErrors()) {
			// アラートメッセージをモデルに設定
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-danger");
			model.addAttribute("message", "パスワード再設定に失敗しました。");
			// パスワード再設定画面を返す
			return "passwordReissue/passwordResetForm";
		}
		// エラーが無かった場合の処理
		// パスワード再設定エンティティを削除
		passwordReissueInfoRepository.delete(info);
		// フォームデータのメールアドレスからユーザーを取得
		User user = repository.findByUsername(email);
		// 取得したユーザーエンティティにフォームデータの新しいパスワードをエンコードして設定
		user.setPassword(passwordEncoder.encode(password));
		// ユーザーエンティティを保存
		repository.saveAndFlush(user);
		// 現在保持している認証情報をクリア
		SecurityContextHolder.clearContext();
		// アラートメッセージをモデルに設定
		model.addAttribute("hasMessage", true);
		model.addAttribute("class", "alert-info");
		model.addAttribute("message", "パスワード再設定が完了しました。");
		// ログイン画面を返す
		return "sessions/new";
	}
}