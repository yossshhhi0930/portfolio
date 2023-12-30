package com.example.portfolio.form;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

import lombok.Data;

@Data

public class CreateReissueInfoForm {

	@NotEmpty
	@Email
	private String Email;
}