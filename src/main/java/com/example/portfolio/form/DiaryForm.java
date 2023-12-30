package com.example.portfolio.form;

import java.time.LocalDate;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import lombok.Data;

@Data
public class DiaryForm {

	private Long id;

	@NotNull
	private Long planId;

	@NotNull
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate record_date;

	@Size(max = 255)
	private String description;

}
