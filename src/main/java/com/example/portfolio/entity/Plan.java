package com.example.portfolio.entity;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

/**
 * 栽培計画エンティティクラス
 */
@Entity
@Table(name = "plan")
@Data
public class Plan extends AbstractEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * デフォルトコンストラクタ
	 */
	public Plan() {
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
	public Plan(Long userId, Long cropId, Long sectionId, LocalDate sowing_date, LocalDate harvest_completion_date,
			boolean completion) {
		this.userId = userId;
		this.cropId = cropId;
		this.sectionId = sectionId;
		this.sowing_date = sowing_date;
		this.harvest_completion_date = harvest_completion_date;
		this.completion = completion;
	}

	/** 栽培計画ID */
	@Id
	@SequenceGenerator(name = "plan_id_seq")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * ユーザーID<br>
	 * 栽培計画の登録者
	 */
	@Column(nullable = false)
	private Long userId;

	/**
	 * 作物ID<br>
	 * 栽培計画を作成する作物
	 */
	@Column(nullable = false)
	private Long cropId;

	/**
	 * 区画ID<br>
	 * 使用する区画
	 */
	@Column(nullable = false)
	private Long sectionId;

	/** 播種日 */
	@Column(nullable = false)
	private LocalDate sowing_date;

	/** 収穫完了予定日 */
	@Column(nullable = false)
	private LocalDate harvest_completion_date;

	/**
	 * 完了<br>
	 * 栽培計画が終了している場合はtrue、終了していない場合はfalse
	 */
	@Column(nullable = false)
	private boolean completion;

	/** 当栽培計画の登録者であるユーザー */
	@ManyToOne
	@JoinColumn(name = "userId", insertable = false, updatable = false)
	private User user;

	/** 当栽培計画が紐づく作物 */
	@ManyToOne
	@JoinColumn(name = "cropId", insertable = false, updatable = false)
	private Crop crop;

	/** 当栽培計画が紐づく区画 */
	@ManyToOne
	@JoinColumn(name = "sectionId", insertable = false, updatable = false)
	private Section section;

	/** 当栽培計画に紐づく栽培日誌リスト */
	@OneToMany(mappedBy = "plan", fetch = FetchType.LAZY)
	private List<Diary> diarys;

}
