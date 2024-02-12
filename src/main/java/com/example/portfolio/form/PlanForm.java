package com.example.portfolio.form;

import java.time.LocalDate;
import lombok.Data;

/**
 * 栽培計画フォームオブジェクトクラス<br>
 * 栽培計画の登録・編集時とガントチャートの表示の際に使用する
 */
@Data
public class PlanForm {

	/**
	 * デフォルトコンストラクタ
	 */
	public PlanForm() {
		super();
	}

	/**
	 * コンストラクタ
	 *
	 * @param userId                  ユーザーID
	 * @param cropId                  作物ID
	 * @param sectionId               区画ID
	 * @param sowing_date             播種日
	 * @param harvest_completion_date 収穫完了予定日
	 * @param completion              完了
	 */
	public PlanForm(Long id, String cropName, String sectionName, LocalDate sowing_date,
			LocalDate harvest_completion_date, boolean completion) {
		this.id = id;
		this.cropName = cropName;
		this.sectionName = sectionName;
		this.sowing_date = sowing_date;
		this.harvest_completion_date = harvest_completion_date;
		this.completion = completion;
	}

	/** 栽培計画ID */
	private Long id;

	/** 作物名 */
	private String cropName;

	/** 区画名 */
	private String sectionName;

	/** 播種日 */
	private LocalDate sowing_date;

	/** 収穫完了予定日 */
	private LocalDate harvest_completion_date;

	/** 完了 */
	private boolean completion;

}
