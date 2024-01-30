package com.example.portfolio.form;

import java.time.LocalDate;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import lombok.Data;

/**
 * 栽培日誌フォームオブジェクトクラス<br>
 * 栽培日誌の登録・編集時に使用する
 */
@Data
public class DiaryForm {

	/** 栽培日誌ID */
	private Long id;

	/** 栽培計画ID */
	@NotNull
	private Long planId;

	/** 記録日 */
	@NotNull
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate record_date;

	/** 説明 */
	@Size(max = 1000)
	private String description;

}
