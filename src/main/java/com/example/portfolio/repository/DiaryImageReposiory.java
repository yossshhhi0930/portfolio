package com.example.portfolio.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.portfolio.entity.Crop;
import com.example.portfolio.entity.CropImage;
import com.example.portfolio.entity.DiaryImage;

@Repository
public interface DiaryImageReposiory extends JpaRepository<DiaryImage, Long> {
//	Iterable<CropImage> findAllByCropIdAndTopImageFalseOrderByUpdatedAtDesc(Long cropId);
//	Optional<CropImage> findById(Long id);
//	List<CropImage> findByCropIdAndTopImageTrue(Long cropId);
//	List<CropImage> findByTopImageTrue();
	List<DiaryImage> findAllByDiaryIdOrderByUpdatedAtDesc(Long diaryId);

}
