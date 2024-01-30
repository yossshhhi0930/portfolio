package com.example.portfolio.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import lombok.Data;

/**
 * 抽象エンティティの基底クラス
 */
@MappedSuperclass
@Data
public class AbstractEntity {

	/** 作成日時 */
	@Column(name = "created_at")
	private Date createdAt;

	/** 更新日時 */
	@Column(name = "updated_at")
	private Date updatedAt;

	/**
	 * エンティティ作成時に、 作成日時と更新日時の設定を行う
	 */
	@PrePersist
	public void onPrePersist() {
		Date date = new Date();
		setCreatedAt(date);
		setUpdatedAt(date);
	}

	/**
	 * エンティティが更新時に、更新日時の設定を行う
	 */
	@PreUpdate
	public void onPreUpdate() {
		setUpdatedAt(new Date());
	}

	/**
	 * ログインユーザーの取得取得
	 *
	 * @return ログインユーザー
	 */
	public User getLoginUser() {
		// TODO: ログインユーザーの取得ロジックを実装する
		return null;
	}
}
