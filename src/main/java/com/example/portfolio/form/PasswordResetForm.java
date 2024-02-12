package com.example.portfolio.form;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import com.example.portfolio.validation.constraints.PasswordEquals;
import lombok.Data;

/**
 * パスワード再発行フォームオブジェクトクラス<br>
 * パスワードの再設定を行う画面で使用するフォーム
 */

@Data
@PasswordEquals
public class PasswordResetForm {

	/** メールアドレス */
	@NotEmpty
	@Email
	private String email;

	/** トークン */
	@NotEmpty
	private String token;

	/** 秘密情報 */
	@NotEmpty
	private String secret;

	/** パスワード */
	@NotEmpty
	@Size(min = 4, max = 20)
	private String password;

	/** 確認用パスワード */
	@NotEmpty
	@Size(min = 4, max = 20)
	private String passwordConfirmation;
}
