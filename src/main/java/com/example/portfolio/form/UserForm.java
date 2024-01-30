package com.example.portfolio.form;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import com.example.portfolio.validation.constraints.PasswordEquals;
import lombok.Data;

/**
 * ユーザーフォームオブジェクトクラス<br>
 * ユーザー登録時に使用するフォーム
 */
@Data
@PasswordEquals
public class UserForm {

	/** ユーザー名 */
	@NotEmpty
	@Size(min = 1, max = 20)
	private String name;

	/** メールアドレス */
	@NotEmpty
	@Email
	private String email;

	/** パスワード */
	@NotEmpty
	@Size(min = 4, max = 20)
	private String password;

	/** 確認用パスワード */
	@NotEmpty
	@Size(min = 4, max = 20)
	private String passwordConfirmation;

}