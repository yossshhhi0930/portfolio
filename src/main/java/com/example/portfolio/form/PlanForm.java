package com.example.portfolio.form;

import java.time.LocalDate;
import lombok.Data;

@Data
public class PlanForm {

	public PlanForm() {
		super();
	}

	public PlanForm(Long id, String cropName, String sectionName, LocalDate sowing_date,
			LocalDate harvest_completion_date, boolean completion) {
		this.id = id;
		this.cropName = cropName;
		this.sectionName = sectionName;
		this.sowing_date = sowing_date;
		this.harvest_completion_date = harvest_completion_date;
		this.completion = completion;
	}

	private Long id;

	private String cropName;

	private String sectionName;

	private LocalDate sowing_date;

	private LocalDate harvest_completion_date;

	private boolean completion;

}
