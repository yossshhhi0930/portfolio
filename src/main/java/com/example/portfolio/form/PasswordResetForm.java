package com.example.portfolio.form;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import com.example.portfolio.validation.constraints.PasswordEquals;
import lombok.Data;

@Data
@PasswordEquals
public class PasswordResetForm {

	@NotEmpty
	@Email
	private String email;

	@NotEmpty
	private String token;

	@NotEmpty
	private String secret;

	@NotEmpty
	@Size(min = 4, max = 20)
	private String password;

	@NotEmpty
	@Size(min = 4, max = 20)
	private String passwordConfirmation;
}
