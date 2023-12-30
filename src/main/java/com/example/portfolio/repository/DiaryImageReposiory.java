package com.example.portfolio.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.portfolio.entity.DiaryImage;

@Repository
public interface DiaryImageReposiory extends JpaRepository<DiaryImage, Long> {
	List<DiaryImage> findAllByDiaryIdOrderByUpdatedAtAsc(Long diaryId);
}
