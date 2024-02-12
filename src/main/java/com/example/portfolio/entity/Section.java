package com.example.portfolio.entity;

import java.io.Serializable;
import java.util.List;
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
 * 区画エンティティクラス
 */
@Entity
@Table(name = "section")
@Data
public class Section extends AbstractEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * デフォルトコンストラクタ
	 */
	public Section() {
		super();
	}

	/**
	 * コンストラクタ
	 *
	 * @param userId      ユーザーID
	 * @param name        区画名
	 * @param description 説明
	 */
	public Section(Long userId, String name, String description) {
		this.userId = userId;
		this.name = name;
		this.description = description;
	}

	/** 区画ID */
	@Id
	@SequenceGenerator(name = "section_id_seq")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * ユーザーID<br>
	 * 区画の登録者
	 */
	@Column(nullable = false)
	private Long userId;

	/** 区画名 */
	@Column(nullable = false)
	private String name;

	/** 説明 */
	@Column
	private String description;

	/** 当区画の登録者であるユーザー */
	@ManyToOne
	@JoinColumn(name = "userId", insertable = false, updatable = false)
	private User user;

	/** 当区画に紐づく栽培計画リスト */
	@OneToMany(mappedBy = "section", fetch = FetchType.LAZY)
	private List<Plan> plans;

}
