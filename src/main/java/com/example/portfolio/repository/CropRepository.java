package com.example.portfolio.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.portfolio.entity.Crop;

/**
 * 作物リポジトリクラス
 */
@Repository
public interface CropRepository extends JpaRepository<Crop, Long> {
	/**
	 * ユーザーIDから、ユーザーに該当する全ての作物エンティティのリストを取得（更新日時の昇順）
	 *
	 * @param userId ユーザーID
	 * @return 該当する作物エンティティのリスト（更新日時の昇順）
	 */
	List<Crop> findAllByUserIdOrderByUpdatedAtAsc(Long userId);

	/**
	 * 作物名とユーザーIDから作物エンティティを取得
	 *
	 * @param name   作物名
	 * @param userId ユーザーID
	 * @return 該当する作物エンティティ
	 */
	Crop findByNameAndUserId(String name, Long userId);

	/**
	 * 作物IDから作物エンティティを取得（Optionalで結果を返す）
	 *
	 * @param Id 作物ID
	 * @return 該当する作物エンティティのOptional
	 */
	Optional<Crop> findById(Long Id);

	/**
	 * 指定のキーワードとユーザーIDから、作物名に指定のキーワードが含まれ、かつユーザーに対応する作物エンティティのリストを取得
	 *
	 * @param keyword 検索キーワード（作物名の部分一致）
	 * @param userId  ユーザーID
	 * @return 該当する作物エンティティのリスト
	 */
	List<Crop> findAllByNameContainingAndUserId(String keyword, Long userId);
}
