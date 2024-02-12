package com.example.portfolio.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.portfolio.entity.Plan;

/**
 * 栽培計画リポジトリクラス
 */
@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {

	/**
	 * ユーザーIDから、ユーザーに対応する現在計画中の全ての栽培計画エンティティのリストを取得（更新日時の昇順）
	 *
	 * @param userId ユーザーID
	 * @return 該当する栽培計画エンティティのリスト（更新日時の昇順）
	 */
	List<Plan> findAllByUserIdAndCompletionFalseOrderByUpdatedAtAsc(Long userId);

	/**
	 * ユーザーIDから、ユーザーに対応する栽培完了済みの全ての栽培計画エンティティのリストを取得（更新日時の昇順）
	 *
	 * @param userId ユーザーID
	 * @return 該当する栽培計画エンティティのリスト（更新日時の昇順）
	 */
	List<Plan> findAllByUserIdAndCompletionTrueOrderByUpdatedAtAsc(Long userId);

	/**
	 * ユーザーIDから、ユーザーに対応する全ての栽培計画エンティティのリストを取得（更新日時の昇順）
	 *
	 * @param userId ユーザーID
	 * @return 該当する栽培計画エンティティのリスト（更新日時の昇順）
	 */
	List<Plan> findAllByUserIdOrderByUpdatedAtAsc(Long userId);

	/**
	 * 指定のキーワードとユーザーIDから、作物名に指定のキーワードが含まれ、かつユーザーに対応する現在計画中の全ての栽培計画エンティティのリストを取得（更新日時の昇順）
	 *
	 * @param userId  ユーザーID
	 * @param keyword 検索キーワード（作物名の部分一致）
	 * @return 該当する栽培計画エンティティのリスト（更新日時の昇順）
	 */
	List<Plan> findAllByUserIdAndCompletionFalseAndCropNameContainingOrderByUpdatedAtAsc(Long userId, String keyword);

	/**
	 * 指定のキーワードとユーザーIDから、作物名に指定のキーワードが含まれ、かつユーザーに対応する栽培完了済みの全ての栽培計画エンティティのリストを取得（更新日時の昇順）
	 *
	 * @param userId  ユーザーID
	 * @param keyword 検索キーワード（作物名の部分一致）
	 * @return 該当する栽培計画エンティティのリスト（更新日時の昇順）
	 */
	List<Plan> findAllByUserIdAndCompletionTrueAndCropNameContainingOrderByUpdatedAtAsc(Long userId, String keyword);

	/**
	 * 指定のキーワードとユーザーIDから、作物名に指定のキーワードが含まれ、かつユーザーに対応する全ての栽培計画エンティティのリストを取得（更新日時の昇順）
	 *
	 * @param userId  ユーザーID
	 * @param keyword 検索キーワード（作物名の部分一致）
	 * @return 該当する栽培計画エンティティのリスト（更新日時の昇順）
	 */
	List<Plan> findAllByUserIdAndCropNameContainingOrderByUpdatedAtAsc(Long userId, String keyword);

	/**
	 * 区画名とユーザーIDから、その区画を使用し、かつユーザーに対応する現在計画中の全ての栽培計画エンティティのリストを取得（更新日時の昇順）
	 *
	 * @param userId      ユーザーID
	 * @param sectionName 区画名
	 * @return 該当する栽培計画エンティティのリスト（更新日時の昇順）
	 */
	List<Plan> findAllByUserIdAndCompletionFalseAndSectionNameOrderByUpdatedAtAsc(Long userId, String sectionName);

	/**
	 * 区画名とユーザーIDから、その区画を使用し、かつユーザーに対応する栽培完了済みの全ての栽培計画エンティティのリストを取得（更新日時の昇順）
	 *
	 * @param userId      ユーザーID
	 * @param sectionName 区画名
	 * @return 該当する栽培計画エンティティのリスト（更新日時の昇順）
	 */
	List<Plan> findAllByUserIdAndCompletionTrueAndSectionNameOrderByUpdatedAtAsc(Long userId, String sectionName);

	/**
	 * 区画名とユーザーIDから、その区画を使用し、かつユーザーに対応する全ての栽培計画エンティティのリストを取得（更新日時の昇順）
	 *
	 * @param userId      ユーザーID
	 * @param sectionName 区画名
	 * @return 該当する栽培計画エンティティのリスト（更新日時の昇順）
	 */
	List<Plan> findAllByUserIdAndSectionNameOrderByUpdatedAtAsc(Long userId, String sectionName);

	/**
	 * 指定のキーワードと区画名とユーザーIDから、作物名に指定のキーワードが含まれ、その区画を使用し、かつユーザーに対応する現在計画中の全ての栽培計画エンティティのリストを取得（更新日時の昇順）
	 *
	 * @param userId      ユーザーID
	 * @param keyword     検索キーワード（作物名の部分一致）
	 * @param sectionName 区画名
	 * @return 該当する栽培計画エンティティのリスト（更新日時の昇順）
	 */
	List<Plan> findAllByUserIdAndCompletionFalseAndCropNameContainingAndSectionNameOrderByUpdatedAtAsc(Long userId,
			String keyword, String sectionName);

	/**
	 * 指定のキーワードと区画名とユーザーIDから、作物名に指定のキーワードが含まれ、その区画を使用し、かつユーザーに対応する栽培完了済みの全ての栽培計画エンティティのリストを取得（更新日時の昇順）
	 *
	 * @param userId      ユーザーID
	 * @param keyword     検索キーワード（作物名の部分一致）
	 * @param sectionName 区画名
	 * @return 該当する栽培計画エンティティのリスト（更新日時の昇順）
	 */
	List<Plan> findAllByUserIdAndCompletionTrueAndCropNameContainingAndSectionNameOrderByUpdatedAtAsc(Long userId,
			String keyword, String sectionName);

	/**
	 * 指定のキーワードと区画名とユーザーIDから、作物名に指定のキーワードが含まれ、その区画を使用し、かつユーザーに対応する全ての栽培計画エンティティのリストを取得（更新日時の昇順）
	 *
	 * @param userId      ユーザーID
	 * @param keyword     検索キーワード（作物名の部分一致）
	 * @param sectionName 区画名
	 * @return 該当する栽培計画エンティティのリスト（更新日時の昇順）
	 */
	List<Plan> findAllByUserIdAndCropNameContainingAndSectionNameOrderByUpdatedAtAsc(Long userId, String keyword,
			String sectionName);
}
