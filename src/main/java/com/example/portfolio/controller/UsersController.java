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

/**
 * ユーザーに関する操作（登録、編集、表示、削除）を担当するコントローラークラス
 *
 */
@Controller
public class UsersController {

	// パスワードエンコーダーの注入
	@Autowired
	private PasswordEncoder passwordEncoder;

	// ユーザーリポジトリの注入
	@Autowired
	private UserRepository repository;

	// メールアドレス変更リポジトリの注入
	@Autowired
	private UserEmailChangeRepository emailChangeRepository;

	// ユーザーに関するメール送信サービスの注入
	@Autowired
	private UserMailSendService userService;

	// URLの有効期限の設定に使用する時間
	@Value("${security.tokenLifeTimeSeconds}")
	int tokenLifeTimeSeconds;

	/**
	 * ユーザー登録画面の表示
	 *
	 * @param model ビューで使用するモデル
	 * @return ユーザー登録画面
	 */
	@GetMapping(path = "/users/new")
	public String newUser(Model model) {
		// ユーザーフォームを初期化
		UserForm form = new UserForm();
		// 初期化したユーザーフォームをモデルのフォームに設定
		model.addAttribute("form", form);
		// ユーザー登録画面を返す
		return "users/new";
	}

	/**
	 * ユーザーの仮登録<br>
	 * ユーザー登録画面から送信されたフォームデータの検証、仮保存、ユーザー本登録用メールの送信を行う
	 *
	 * @param form   ユーザー登録フォームデータ
	 * @param result フォーム検証の結果
	 * @param model  ビューで使用するモデル
	 * @return 検証結果に基づく画面
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	@PostMapping(path = "/user")
	public String precreate(@Validated @ModelAttribute("form") UserForm form, BindingResult result, Model model)
			throws IOException {
		// フォームデータを変数に代入
		String name = form.getName();
		String email = form.getEmail();
		String password = form.getPassword();
		String token = UUID.randomUUID().toString();
		// メールアドレスが既に登録されいる場合に返すエラーの追加
		if (repository.findByUsername(email) != null) {
			FieldError fieldError = new FieldError(result.getObjectName(), "email", "そのメールアドレスは既に登録されています。");
			result.addError(fieldError);
		}
		// エラーがあった場合に、ユーザー登録画面を返し、エラーメッセージを表示
		if (result.hasErrors()) {
			// アラートメッセージをモデルに設定
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-danger");
			model.addAttribute("message", "ユーザーの登録に失敗しました。");
			// ユーザー登録画面を返す
			return "users/new";
		}
		// エラーが無かった場合の処理
		// フォームデータでユーザーエンティティの生成（まだ無効であるため、enabledの値を”false"に設定）
		User entity = new User(email, name, passwordEncoder.encode(password), Authority.ROLE_USER, token, false);
		// ユーザーエンティティの保存
		repository.saveAndFlush(entity);
		// ユーザー本登録案内メールの送信
		userService.sendVerifyMail(name, email, token);
		// アラートメッセージをモデルに設定
		model.addAttribute("hasMessage", true);
		model.addAttribute("class", "alert-info");
		model.addAttribute("message", "ユーザーの仮登録が完了しました。");
		// モデルにユーザー仮登録完了メッセージを設定
		model.addAttribute("uniqueMessage", "ご登録いただいたメールアドレスに、ユーザー本登録案内メールをお送りいたしました。メール添付リンクにアクセスし、本登録の完了をお願いいたします。");
		// ユーザー仮登録完了画面を返す
		return "pages/default";
	}

	/**
	 * ユーザーの本登録<br>
	 * ユーザー本登録案内メールに添付したURLがクリックされた際に、URLの検証、ユーザー本登録を行う
	 *
	 * @param token URLの検証に使用するトークン
	 * @param model ビューで使用するモデル
	 * @return 検証結果に基づく画面
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	@GetMapping(path = "/verify/{token}")
	public String verify(@PathVariable String token, Model model) throws IOException {
		// トークンからユーザーを取得
		User entity = repository.findByToken(token);
		// ユーザーが既に有効になっているか、URLに含まれるトークンに誤りがあるか、URLの有効期限が切れている場合の処理
		if (entity == null || entity.isEnabled()) {
			// アラートメッセージをモデルに設定
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-danger");
			model.addAttribute("message", "URLが無効です。");
			// トップ画面を返す
			return "pages/index";
		}
		// エラーが無かった場合の処理
		// ユーザーを有効に更新して、保存
		entity.setEnabled(true);
		repository.saveAndFlush(entity);
		// アラートメッセージをモデルに設定
		model.addAttribute("hasMessage", true);
		model.addAttribute("class", "alert-info");
		model.addAttribute("message", "ユーザーの登録が完了しました。");
		// ログイン画面を返す
		return "sessions/new";
	}

	/**
	 * ユーザー詳細画面を表示
	 *
	 * @param principal 認証情報
	 * @param model     ビューで使用するモデル
	 * @return ユーザー詳細画面
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	@GetMapping(path = "/users/detail")
	public String showDetail(Principal principal, Model model) throws IOException {
		// 認証情報からユーザーを取得
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		User entity = repository.findByUserId(user.getUserId());
		// 取得したユーザー情報を、モデルに設定
		model.addAttribute("user", entity);
		// ユーザー詳細画面を返す
		return "users/detail";
	}

	/**
	 * ユーザー編集画面を表示
	 *
	 * @param principal 認証情報
	 * @param model     ビューで使用するモデル
	 * @return ユーザー編集画面
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	@GetMapping(path = "/users/edit")
	public String showEditPage(Principal principal, Model model) throws IOException {
		// 認証情報からユーザーを取得
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		User entity = repository.findByUserId(user.getUserId());
		// ユーザーからユーザー編集フォームを生成
		UserEditForm form = getUserEditForm(entity);
		// 取得したユーザー編集フォームをモデルに設定
		model.addAttribute("form", form);
		// ユーザー編集画面を返す
		return "users/edit";
	}

	/**
	 * ユーザーの編集<br>
	 * ユーザー編集画面から送信されたフォームデータの検証、更新または更新前処理を行う
	 *
	 * @param principal  認証情報
	 * @param form       ユーザー編集フォームデータ
	 * @param result     フォーム検証の結果
	 * @param model      ビューで使用するモデル
	 * @param redirAttrs リダイレクト属性
	 * @return 検証結果に基づく画面
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	@PostMapping(path = "/users/edit-complete")
	public String edit(Principal principal, @Validated @ModelAttribute("form") UserEditForm form, BindingResult result,
			Model model, RedirectAttributes redirAttrs) throws IOException {
		// 認証情報からユーザーを取得
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		User entity = repository.findByUserId(user.getUserId());
		// フォームデータを変数に代入
		String name = form.getName();
		String email = form.getEmail();
		// メールアドレスが既に登録されいる場合に返すエラーの追加
		if (!email.equals(entity.getUsername()) && repository.findByUsername(email) != null) {
			FieldError fieldError = new FieldError(result.getObjectName(), "email", "そのメールアドレスは既に登録されています。");
			result.addError(fieldError);
		}
		// エラーがあった場合に、ユーザー編集画面を返し、エラーメッセージを表示
		if (result.hasErrors()) {
			// アラートメッセージをモデルに設定
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-danger");
			model.addAttribute("message", "ユーザーの編集に失敗しました。");
			// ユーザー編集画面を返す
			return "users/edit";
		}
		// エラーが無かった場合の処理
		// メールアドレスに変更がある場合の処理
		if (!email.equals(entity.getUsername())) {
			// ユーザー名にも変更があった場合、ユーザー名を先に更新
			entity.setName(name);
			// ユーザーエンティティを保存
			repository.saveAndFlush(entity);
			// 新しいメールアドレス一時保存用にメールアドレス変更エンティティの生成・保存
			// トークンの生成
			String token = UUID.randomUUID().toString();
			// 有効期限を生成
			LocalDateTime expiryDate = LocalDateTime.now().plusSeconds(tokenLifeTimeSeconds);
			// メールアドレス変更エンティティを生成・保存
			UserEmailChange emailChangeEntity = new UserEmailChange(entity.getUserId(), token, expiryDate, email);
			emailChangeRepository.saveAndFlush(emailChangeEntity);
			// 新しいメールアドレス宛てに、メールアドレス変更本手続き案内メールを送信
			userService.sendEmailVerifMail(name, email, token);
			// アラートメッセージをモデルに設定
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-info");
			model.addAttribute("message", "ユーザー名の変更及び、メールアドレス変更仮手続きが完了しました。");
			// モデルにメールアドレス変更仮手続き完了メッセージを設定
			model.addAttribute("uniqueMessage",
					"ご登録いただいた新しいメールアドレスに、メールアドレス変更本手続き案内メールをお送りいたしました。メール添付リンクにアクセスし、手続きの完了をお願いいたします。");
			// メールアドレス変更仮手続き完了画面を返す
			return "pages/default";
		}
		// メールアドレスに変更がない場合の処理
		// ユーザー名に変更があった場合、ユーザー名を更新
		entity.setName(name);
		// アラートメッセージをリダイレクト先のモデルに設定
		redirAttrs.addFlashAttribute("hasMessage", true);
		redirAttrs.addFlashAttribute("class", "alert-info");
		redirAttrs.addFlashAttribute("message", "ユーザーの編集が完了しました。");
		// リダイレクト先で、ユーザー詳細画面を返す
		return "redirect:/users/detail";
	}

	/**
	 * メールアドレス変更本登録<br>
	 * メールアドレス変更本手続き案内メールに添付したURLがクリックされた際に、URLの検証、メールアドレスの更新を行う
	 *
	 * @param token      URLの検証に使用するトークン
	 * @param model      ビューで使用するモデル
	 * @param redirAttrs リダイレクト属性
	 * @return 検証結果に基づく画面
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	@GetMapping(path = "/email-verify/{token}")
	public String emailVerify(@PathVariable String token, Model model, RedirectAttributes redirAttrs)
			throws IOException {
		// トークンよりメールアドレス変更エンティティを取得
		UserEmailChange userEmailChange = emailChangeRepository.findByToken(token);
		// トークンに誤りがある場合にエラーを返す
		if (userEmailChange == null) {
			// アラートメッセージをモデルに設定
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-danger");
			model.addAttribute("message", "URLが無効です。");
			// トップ画面を返す
			return "pages/index";
		}
		// URLの有効期限が切れている場合にエラーを返す
		if (LocalDateTime.now().isAfter(userEmailChange.getExpiryDate())) {
			// アラートメッセージをモデルに設定
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-danger");
			model.addAttribute("message", "URLの有効期限が切れています。");
			// トップ画面を返す
			return "pages/index";
		}
		// エラーが無かった場合の処理
		// メールアドレス変更エンティティのユーザーIDからユーザーを取得
		User entity = repository.findByUserId(userEmailChange.getUserId());
		// 新しいメールアドレスに更新・保存
		entity.setUsername(userEmailChange.getNewEmail());
		repository.saveAndFlush(entity);
		// メールアドレス変更エンティティを削除
		emailChangeRepository.delete(userEmailChange);
		// アラートメッセージをリダイレクト先のモデルに設定
		redirAttrs.addFlashAttribute("hasMessage", true);
		redirAttrs.addFlashAttribute("class", "alert-info");
		redirAttrs.addFlashAttribute("message", "メールアドレス変更手続きが完了しました。");
		// リダイレクト先で、ユーザー詳細画面を返す
		return "redirect:/users/detail";
	}

	/**
	 * ユーザーの削除
	 *
	 * @param principal  認証情報
	 * @param redirAttrs リダイレクト属性
	 * @param model      ビューで使用するモデル
	 * @return トップ画面
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	@GetMapping(path = "/users/delete")
	public String delete(Principal principal, RedirectAttributes redirAttrs, Model model) throws IOException {
		// 認証情報からユーザーを取得
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		User entity = repository.findByUserId(user.getUserId());
		// ユーザーを削除
		repository.delete(entity);
		// アラートメッセージをモデルに設定
		model.addAttribute("hasMessage", true);
		model.addAttribute("class", "alert-info");
		model.addAttribute("message", "ユーザーの削除が完了しました。");
		// トップ画面を返す
		return "pages/index";
	}

	/**
	 * ユーザーエンティティからユーザー編集フォームオブジェクトを生成するメソッド
	 *
	 * @param user ユーザーエンティティ
	 * @return 生成されたユーザー編集フォームオブジェクト
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	public UserEditForm getUserEditForm(User user) throws IOException {
		// ユーザー編集フォームを初期化
		UserEditForm form = new UserEditForm();
		// 初期化したユーザー編集フォームにユーザーエンティティのユーザー名とメールアドレスを設定し、返す
		form.setName(user.getName());
		form.setEmail(user.getUsername());
		return form;
	}
}
