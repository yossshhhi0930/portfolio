package com.example.portfolio.entity;

import java.io.Serializable;
import java.util.Date;
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
import lombok.Data;

/**
 * 作物エンティティクラス
 */
@Entity
@Table(name = "crop")
@Data
public class Crop extends AbstractEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * デフォルトコンストラクタ
	 */
	public Crop() {
		super();
	}

	/**
	 * コンストラクタ
	 *
	 * @param userId              ユーザーID
	 * @param name                作物名
	 * @param sowing_start        播種可能期間開始日
	 * @param sowing_end          播種可能期間終了日
	 * @param cultivationp_period 栽培日数
	 * @param manual              栽培手順説明文
	 */
	public Crop(Long userId, String name, Date sowing_start, Date sowing_end, int cultivationp_period, String manual) {
		this.userId = userId;
		this.name = name;
		this.sowing_start = sowing_start;
		this.sowing_end = sowing_end;
		this.cultivationp_period = cultivationp_period;
		this.manual = manual;
	}

	/** 作物ID */
	@Id
	@SequenceGenerator(name = "crop_id_seq")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * ユーザーID<br>
	 * 作物の登録者
	 */
	@Column(nullable = false)
	private Long userId;

	/** 作物名 */
	@Column(nullable = false)
	private String name;

	/** 播種可能期間開始日 */
	@Column(nullable = false)
	private Date sowing_start;

	/** 播種可能期間終了日 */
	@Column(nullable = false)
	private Date sowing_end;

	/** 栽培日数 */
	@Column(nullable = false)
	private int cultivationp_period;

	/** 栽培手順説明文 */
	@Column
	private String manual;

	/** 当作物の登録者であるユーザー */
	@ManyToOne
	@JoinColumn(name = "userId", insertable = false, updatable = false)
	private User user;

	/** 当作物に紐づく作物画像リスト */
	@OneToMany(mappedBy = "crop", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
	private List<CropImage> cropImages;

	/** 当作物に紐づく栽培計画リスト */
	@OneToMany(mappedBy = "crop", fetch = FetchType.LAZY)
	private List<Plan> plans;

}
