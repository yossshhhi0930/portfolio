package com.example.portfolio.form;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

import lombok.Data;

/**
 * パスワード再発行認証情報作成フォームオブジェクトクラス<br>
 * パスワードの再発行に際し、パスワード再発行URLを送る先のメールアドレスを入力する画面で使用するフォーム
 */
@Data
public class CreateReissueInfoForm {

	/** パスワード再発行用URLを送る先のメールアドレス */
	@NotEmpty
	@Email
	private String email;
}