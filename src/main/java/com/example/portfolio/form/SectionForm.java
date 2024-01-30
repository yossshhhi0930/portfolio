package com.example.portfolio.form;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import lombok.Data;

/**
 * 区画フォームオブジェクトクラス<br>
 * 区画登録・編集時に使用するフォーム
 */
@Data
public class SectionForm {

	/** 区画ID */
	private Long id;

	/** 区画名 */
	@NotEmpty
	@Size(min = 1, max = 20)
	private String name;

	/** 説明 */
	@Size(max = 255)
	private String description;

}
