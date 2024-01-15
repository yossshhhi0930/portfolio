package com.example.portfolio.entity;

import lombok.Data;

@Data
public class DisplayCrop {

	private Long id;

	private Long userId;

	private String name;

	private String sowing_start;

	private String sowing_end;

	private int cultivationp_period;

	private String manual;
}
