package com.example.portfolio.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.example.portfolio.entity.User;
import com.example.portfolio.repository.UserRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ユーザー詳細サービスの実装クラス
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	// ユーザーリポジトリの注入
	@Autowired
	private UserRepository repository;

	// ログ出力のためのLogger
	protected static Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

	/**
	 * メールアドレスからユーザー詳細情報を取得
	 *
	 * @param username ユーザーのメールアドレス
	 * @return ユーザー詳細
	 * @throws UsernameNotFoundException ユーザーが見つからない場合にスロー
	 */
	@Override
	@Transactional
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		// メールアドレスが空の場合は例外をスロー
		if (username == null || "".equals(username)) {
			throw new UsernameNotFoundException("Username is empty");
		}

		// メールアドレスからユーザを取得
		User entity = repository.findByUsername(username);
		// ユーザが存在しない場合は例外をスロー
		if (entity == null) {
			throw new UsernameNotFoundException("User not found with username: " + username);
		}

		// ユーザエンティティからSpring SecurityのUserDetailsを構築して返す
		return buildUserDetails(entity);
	}

	/**
	 * ユーザーエンティティからSpring SecurityのUserDetailsを構築
	 *
	 * @param user ユーザーエンティティ
	 * @return UserDetails
	 */
	private UserDetails buildUserDetails(User user) {
		return org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
				.password(user.getPassword()).authorities(mapAuthorities(user.getAuthority()))
				.disabled(!user.isEnabled()).accountExpired(!user.isAccountNonExpired())
				.accountLocked(!user.isAccountNonLocked()).credentialsExpired(!user.isCredentialsNonExpired()).build();
	}

	/**
	 * ユーザー権限をSpring SecurityのGrantedAuthorityにマップ
	 *
	 * @param authority ユーザー権限
	 * @return マップされた権限のコレクション
	 */
	private Collection<? extends GrantedAuthority> mapAuthorities(User.Authority authority) {
		List<GrantedAuthority> authorities = new ArrayList<>();
		authorities.add(new SimpleGrantedAuthority(authority.toString()));
		return authorities;
	}
}