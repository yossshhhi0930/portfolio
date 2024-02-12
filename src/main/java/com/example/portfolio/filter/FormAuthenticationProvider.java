package com.example.portfolio.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.portfolio.entity.User;
import com.example.portfolio.repository.UserRepository;

/**
 * フォーム認証プロバイダークラス
 */
@Configuration
public class FormAuthenticationProvider implements AuthenticationProvider {

	// ロガーの初期化
	protected static Logger log = LoggerFactory.getLogger(FormAuthenticationProvider.class);

	// ユーザーリポジトリの注入
	@Autowired
	private UserRepository repository;

	// パスワードエンコーダーの注入
	@Autowired
	@Lazy
	private PasswordEncoder passwordEncoder;

	/**
	 * ユーザーの認証
	 *
	 * @param auth 認証情報
	 * @return 認証トークン
	 * @throws AuthenticationException 認証に失敗した場合の例外
	 */
	@Override
	public Authentication authenticate(Authentication auth) throws AuthenticationException {
		// 認証情報から、メールアドレスとパスワードを取得
		String name = auth.getName();
		String password = auth.getCredentials().toString();
		// メールアドレスをログに出力
		log.debug("name={}", name);
		// パスワードをログに出力
		log.debug("password={}", password);

		// メールアドレスとパスワードが入力されていない場合はエラー
		if ("".equals(name) || "".equals(password)) {
			throw new AuthenticationCredentialsNotFoundException("ログイン情報に不備があります。");
		}

		// メールアドレスからユーザーエンティティを取得
		User entity = repository.findByUsername(name);
		// ユーザーが存在しない場合はエラー
		if (entity == null) {
			throw new AuthenticationCredentialsNotFoundException("ログイン情報が存在しません。");
		}

		// パスワードが一致しない場合はエラー
		if (!passwordEncoder.matches(password, entity.getPassword())) {
			throw new AuthenticationCredentialsNotFoundException("ログイン情報に不備があります。");
		}

		// ユーザーが有効でない場合はエラー
		if (!entity.isEnabled()) {
			throw new AuthenticationCredentialsNotFoundException("このユーザーは有効ではありません。");
		}
		// 認証トークンを作成して返す
		return new UsernamePasswordAuthenticationToken(entity, password, entity.getAuthorities());
	}

	/**
	 * このプロバイダーが、指定された認証オブジェクトをサポートするかどうかを判定
	 *
	 * @param authentication 認証オブジェクト
	 * @return このプロバイダーがサポートする場合はtrue、それ以外はfalse
	 */
	@Override
	public boolean supports(Class<?> authentication) {
		return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
	}
}