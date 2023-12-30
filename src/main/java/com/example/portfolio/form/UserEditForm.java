package com.example.portfolio.form;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class UserEditForm {

	@NotEmpty
	@Size(min = 1, max = 20)
	private String name;

	@NotEmpty
	@Email
	private String email;

}