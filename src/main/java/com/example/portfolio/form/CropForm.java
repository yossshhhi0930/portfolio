package com.example.portfolio.form;

import java.util.Date;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class CropForm {

	private Long id;

	@NotEmpty
	@Size(min = 1, max = 20)
	private String name;

	@NotNull
	private Date sowing_start;

	@NotNull
	private Date sowing_end;

	@Min(value = 1)
	private int cultivationp_period;

	@Size(max = 1000)
	private String manual;
}
