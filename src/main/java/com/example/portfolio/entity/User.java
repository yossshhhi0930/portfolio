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

@Entity
@Table(name = "users")
@Data
public class User extends AbstractEntity implements UserDetails, UserInf {
	private static final long serialVersionUID = 1L;

	public enum Authority {
		ROLE_USER, ROLE_ADMIN
	};

	public User() {
		super();
	}

	public User(String email, String name, String password, Authority authority, String token, boolean enabled) {
		this.username = email;
		this.name = name;
		this.password = password;
		this.authority = authority;
		this.token = token;
		this.enabled = enabled;
	}

	@Id
	@SequenceGenerator(name = "users_id_seq")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long userId;

	@Column(nullable = false, unique = true)
	private String username;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String password;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private Authority authority;

	// ユーザー本登録用URLに含めるために生成される文字列（トークン）
	private String token;

	// 初期値はfalse 認証が成功すると trueになる。
	@Column(nullable = false)
	private boolean enabled;

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		List<GrantedAuthority> authorities = new ArrayList<>();
		authorities.add(new SimpleGrantedAuthority(authority.toString()));
		return authorities;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	// enabledの値を返す。
	@Override
	public boolean isEnabled() {
		return enabled;
	}

}
