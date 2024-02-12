package com.example.portfolio.entity;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.Data;

/**
 * 栽培日誌画像エンティティクラス
 */
@Entity
@Table(name = "diary_image")
@Data
public class DiaryImage extends AbstractEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * デフォルトコンストラクタ
	 */
	public DiaryImage() {
		super();
	}

	/**
	 * コンストラクタ
	 *
	 * @param diaryId 栽培日誌ID
	 * @param path    栽培日誌画像パス
	 */
	public DiaryImage(Long diaryId, String path) {
		this.diaryId = diaryId;
		this.path = path;
	}

	/** 栽培日誌画像ID */
	@Id
	@SequenceGenerator(name = "diary_image_id_seq")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** 栽培日誌ID */
	@Column(nullable = false)
	private Long diaryId;

	/** 栽培日誌画像パス */
	@Column(nullable = false)
	private String path;

	/** 当栽培日誌画像が紐づく栽培日誌 */
	@ManyToOne
	@JoinColumn(name = "diaryId", insertable = false, updatable = false)
	private Diary diary;
}
