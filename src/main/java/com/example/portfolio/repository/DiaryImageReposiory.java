package com.example.portfolio.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.portfolio.entity.DiaryImage;

/**
 * 栽培日誌画像リポジトリクラス
 */
@Repository
public interface DiaryImageReposiory extends JpaRepository<DiaryImage, Long> {
	/**
	 * 栽培日誌IDからその栽培日誌の全ての栽培日誌画像エンティティを取得（更新日時の昇順）
	 *
	 * @param diaryId 栽培日誌ID
	 * @return 該当する栽培日誌画像エンティティのリスト（更新日時の昇順）
	 */
	List<DiaryImage> findAllByDiaryIdOrderByUpdatedAtAsc(Long diaryId);
}
