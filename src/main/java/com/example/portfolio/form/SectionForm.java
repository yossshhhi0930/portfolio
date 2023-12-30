package com.example.portfolio.form;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class SectionForm {

	private Long id;

	@NotEmpty
	@Size(min = 1, max = 20)
	private String name;

	@Size(max = 255)
	private String description;

}
