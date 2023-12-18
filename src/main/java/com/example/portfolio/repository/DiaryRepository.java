package com.example.portfolio.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.portfolio.entity.Crop;
import com.example.portfolio.entity.Diary;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {
	List<Diary> findAllByUserIdOrderByUpdatedAtDesc(Long userId);
	List<Diary> findAllByPlanIdOrderByUpdatedAtDesc(Long planId);
//	Crop findByNameAndUserId(String name, Long userId);
	Optional<Diary> findById(Long Id);
//	List<Crop>findByNameContainingAndUserId(String keyword, Long userId);
//	void saveAndFlush(Optional<Crop> entity);
}
