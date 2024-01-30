package com.example.portfolio.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.portfolio.entity.CropImage;

/**
 * 作物画像リポジトリクラス
 */
@Repository
public interface CropImageReposiory extends JpaRepository<CropImage, Long> {
	/**
	 * 作物画像IDから作物画像エンティティを取得（Optionalで結果を返す）
	 *
	 * @param id 作物画像ID
	 * @return 該当する作物画像エンティティのOptional
	 */
	Optional<CropImage> findById(Long id);

	/**
	 * 作物IDから、その作物のトップ画像である作物画像エンティティのリストを取得
	 *
	 * @param cropId 作物ID
	 * @return 該当する作物画像エンティティのリスト
	 */
	List<CropImage> findByCropIdAndTopImageTrue(Long cropId);

	/**
	 * 作物IDから、その作物の全ての作物画像エンティティのリストを取得（更新日時の昇順）
	 *
	 * @param cropId 作物ID
	 * @return 該当する作物画像エンティティのリスト（更新日時の昇順）
	 */
	List<CropImage> findAllByCropIdOrderByUpdatedAtAsc(Long cropId);
}
