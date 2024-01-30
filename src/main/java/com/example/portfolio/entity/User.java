package com.example.portfolio.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import lombok.Data;

/**
 * ユーザーエンティティクラス
 */
@Entity
@Table(name = "users")
@Data
public class User extends AbstractEntity implements UserDetails, UserInf {
	private static final long serialVersionUID = 1L;

	/**
	 * 権限の列挙型（ユーザーと管理者）
	 */
	public enum Authority {
		ROLE_USER, ROLE_ADMIN
	};

	/**
	 * デフォルトコンストラクタ
	 */
	public User() {
		super();
	}

	/**
	 * コンストラクタ
	 *
	 * @param email     メールアドレス
	 * @param name      ユーザー名
	 * @param password  パスワード
	 * @param authority 権限
	 * @param token     トークン
	 * @param enabled   有効
	 */
	public User(String email, String name, String password, Authority authority, String token, boolean enabled) {
		this.username = email;
		this.name = name;
		this.password = password;
		this.authority = authority;
		this.token = token;
		this.enabled = enabled;
	}

	/** ユーザーID */
	@Id
	@SequenceGenerator(name = "users_id_seq")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long userId;

	/** メールアドレス */
	@Column(nullable = false, unique = true)
	private String username;

	/** ユーザー名 */
	@Column(nullable = false)
	private String name;

	/** パスワード */
	@Column(nullable = false)
	private String password;

	/** 権限 */
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private Authority authority;

	/**
	 * トークン<br>
	 * ユーザー本登録URLに含め、認証に使用する文字列
	 */
	@Column
	private String token;

	/**
	 * 有効<br>
	 * 初期値はfalse 認証が成功すると trueになる
	 */
	@Column(nullable = false)
	private boolean enabled;

	/**
	 * ユーザーの権限を返す
	 *
	 * @return 権限のリスト
	 */
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		List<GrantedAuthority> authorities = new ArrayList<>();
		authorities.add(new SimpleGrantedAuthority(authority.toString()));
		return authorities;
	}

	/**
	 * アカウントの有効期限が切れているかどうかを返す
	 *
	 * @return 有効期限内であればtrue
	 */
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	/**
	 * アカウントがロックされているかどうかを返す
	 *
	 * @return ロックされていなければtrue
	 */
	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	/**
	 * パスワードの有効期限が切れているかどうかを返す
	 *
	 * @return 有効期限内であればtrue
	 */
	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	/**
	 * アカウントが有効かどうかを返す
	 *
	 * @return 有効であればtrue
	 */
	@Override
	public boolean isEnabled() {
		return enabled;
	}

}
