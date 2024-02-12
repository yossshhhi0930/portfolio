package com.example.portfolio.entity;

import java.io.Serializable;
import java.util.List;
import javax.persistence.CascadeType;
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
import lombok.Data;

/**
 * 栽培日誌エンティティクラス
 */
@Entity
@Table(name = "diary")
@Data
public class Diary extends AbstractEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * デフォルトコンストラクタ
	 */
	public Diary() {
		super();
	}

	/**
	 * コンストラクタ
	 *
	 * @param userId      ユーザーID
	 * @param planId      栽培計画ID
	 * @param record_date 記録日
	 * @param description 説明
	 */
	public Diary(Long userId, Long planId, LocalDate record_date, String description) {
		this.userId = userId;
		this.planId = planId;
		this.record_date = record_date;
		this.description = description;
	}

	/** 栽培日誌ID */
	@Id
	@SequenceGenerator(name = "diary_id_seq")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * ユーザーID<br>
	 * 栽培日誌の登録者
	 */
	@Column(nullable = false)
	private Long userId;

	/**
	 * 栽培計画ID<br>
	 * 栽培日誌の対象となる栽培計画
	 */
	@Column(nullable = false)
	private Long planId;

	/** 記録日 */
	@Column(nullable = false)
	private LocalDate record_date;

	/** 説明 */
	@Column
	private String description;

	/** 当栽培日誌の登録者であるユーザー */
	@ManyToOne
	@JoinColumn(name = "userId", insertable = false, updatable = false)
	private User user;

	/** 当栽培日誌に紐づく栽培日誌画像リスト */
	@OneToMany(mappedBy = "diary", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
	private List<DiaryImage> diaryImages;

	/** 当栽培日誌が紐づく栽培計画 */
	@ManyToOne
	@JoinColumn(name = "planId", insertable = false, updatable = false)
	private Plan plan;

}
