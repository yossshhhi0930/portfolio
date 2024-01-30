package com.example.portfolio.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.portfolio.entity.Diary;

/**
 * 栽培日誌リポジトリクラス
 */
@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {
	/**
	 * 栽培日誌IDから栽培日誌エンティティを取得（Optionalで結果を返す）
	 *
	 * @param id 栽培日誌ID
	 * @return 該当する栽培日誌エンティティのOptional
	 */
	Optional<Diary> findById(Long Id);

	/**
	 * ユーザーIDから、ユーザーに対応する全ての栽培日誌エンティティのリストを取得（更新日時の昇順）
	 *
	 * @param userId ユーザーID
	 * @return 該当する栽培日誌エンティティのリスト（更新日時の昇順）
	 */
	List<Diary> findAllByUserIdOrderByUpdatedAtAsc(Long userId);

	/**
	 * 栽培計画IDからその栽培計画に紐づく全ての栽培日誌エンティティのリストを取得（更新日時の昇順）
	 *
	 * @param planId 栽培計画ID
	 * @return 該当する栽培日誌エンティティのリスト（更新日時の昇順）
	 */
	List<Diary> findAllByPlanIdOrderByUpdatedAtAsc(Long planId);
}
