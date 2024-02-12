package com.example.portfolio.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.portfolio.entity.Section;

/**
 * 区画リポジトリクラス
 */
@Repository
public interface SectionRepository extends JpaRepository<Section, Long> {

	/**
	 * 区画名とユーザーIDから区画エンティティを取得
	 *
	 * @param name   区画名
	 * @param userId ユーザーID
	 * @return 該当する区画エンティティ
	 */
	Section findByNameAndUserId(String name, Long userId);

	/**
	 * 区画IDから区画エンティティを取得（Optionalで結果を返す）
	 *
	 * @param id 区画ID
	 * @return 該当する区画エンティティのOptional
	 */
	Optional<Section> findById(Long id);

	/**
	 * 指定のキーワードとユーザーIDから、区画名に指定のキーワードが含まれ、かつユーザーに対応する区画エンティティのリストを取得
	 *
	 * @param keyword キーワード
	 * @param userId  ユーザーID
	 * @return 該当する区画エンティティのリスト
	 */
	List<Section> findAllByNameContainingAndUserId(String keyword, Long userId);

	/**
	 * ユーザーIDから、ユーザーに該当する全ての区画エンティティのリストを取得（更新日時の昇順）
	 *
	 * @param userId ユーザーID
	 * @return 該当する区画エンティティのリスト（更新日時の昇順）
	 */
	List<Section> findAllByUserIdOrderByUpdatedAtAsc(Long userId);
}
