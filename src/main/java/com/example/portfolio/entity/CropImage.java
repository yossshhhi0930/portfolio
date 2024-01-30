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
 * 作物画像エンティティクラス
 */
@Entity
@Table(name = "crop_image")
@Data
public class CropImage extends AbstractEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * デフォルトコンストラクタ
	 */
	public CropImage() {
		super();
	}

	/**
	 * コンストラクタ
	 *
	 * @param cropId   作物ID
	 * @param path     作物画像パス
	 * @param topImage トップ画像
	 */
	public CropImage(Long cropId, String path, boolean topImage) {
		this.cropId = cropId;
		this.path = path;
		this.topImage = topImage;
	}

	/** 作物画像ID */
	@Id
	@SequenceGenerator(name = "crop_image_id_seq")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** 作物ID */
	@Column(nullable = false)
	private Long cropId;

	/** 作物画像パス */
	@Column(nullable = false)
	private String path;

	/**
	 * トップ画像<br>
	 * トップ画像である場合はtrue、その他画像である場合はfalse
	 */
	@Column(nullable = false)
	private boolean topImage;

	/** 当作物画像が紐づく作物 */
	@ManyToOne
	@JoinColumn(name = "cropId", insertable = false, updatable = false)
	private Crop crop;
}
