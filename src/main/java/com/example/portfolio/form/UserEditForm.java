package com.example.portfolio.form;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import lombok.Data;

/**
 * ユーザー編集フォームオブジェクトクラス<br>
 * ユーザー編集時に使用するフォーム
 */
@Data
public class UserEditForm {

	/** ユーザー名 */
	@NotEmpty
	@Size(min = 1, max = 20)
	private String name;

	/** メールアドレス */
	@NotEmpty
	@Email
	private String email;

}