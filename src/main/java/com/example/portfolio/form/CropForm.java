package com.example.portfolio.form;

import java.util.Date;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

/**
 * 作物フォームオブジェクトクラス<br>
 * 作物登録・編集時に使用するフォーム
 */
@Data
public class CropForm {

	/** 作物ID */
	private Long id;

	/** 作物名 */
	@NotEmpty
	@Size(min = 1, max = 20)
	private String name;

	/** 播種可能期間開始日 */
	@NotNull
	private Date sowing_start;

	/** 播種可能期間終了日 */
	@NotNull
	private Date sowing_end;

	/** 栽培日数 */
	@Min(value = 1)
	private int cultivationp_period;

	/** 栽培手順説明文 */
	@Size(max = 1000)
	private String manual;
}
