package com.example.portfolio.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.portfolio.entity.Crop;
import com.example.portfolio.entity.Plan;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {
	//Iterable<Crop>findAllByUserIdOrderByUpdatedAtDesc(Long userId);
	//Crop findByCropId(String cropId);
	//Optional<Crop> findById(Long Id);
	//List<Crop>findByNameContaining(String keyword);
	//void saveAndFlush(Optional<Crop> entity);
	List<Plan>findAllByUserIdOrderByUpdatedAtDesc(Long userId);
	List<Plan>findAllBySectionIdOrderByUpdatedAtDesc(Long sectionId);
	List<Plan>findAllByUserIdAndCompletionTrueOrderByUpdatedAtDesc(Long userId);
	List<Plan>findAllByUserIdAndCompletionFalseOrderByUpdatedAtDesc(Long userId);
	List<Plan>findAllByUserIdAndCropNameContainingOrderByUpdatedAtDesc(Long userId, String keyword);
	List<Plan>findAllByUserIdAndCompletionTrueAndCropNameContainingOrderByUpdatedAtDesc(Long userId, String keyword);
	List<Plan>findAllByUserIdAndCompletionFalseAndCropNameContainingOrderByUpdatedAtDesc(Long userId, String keyword);
	List<Plan>findAllByUserIdAndCompletionFalseAndSectionNameOrderByUpdatedAtDesc(Long userId, String sectionName);
	List<Plan>findAllByUserIdAndCompletionTrueAndSectionNameOrderByUpdatedAtDesc(Long userId, String sectionName);
	List<Plan>findAllByUserIdAndSectionNameOrderByUpdatedAtDesc(Long userId, String sectionName);
	List<Plan>findAllByUserIdAndCropNameContainingAndSectionNameOrderByUpdatedAtDesc(Long userId, String keyword , String sectionName);
	List<Plan>findAllByUserIdAndCompletionTrueAndCropNameContainingAndSectionNameOrderByUpdatedAtDesc(Long userId, String keyword , String sectionName);
	List<Plan>findAllByUserIdAndCompletionFalseAndCropNameContainingAndSectionNameOrderByUpdatedAtDesc(Long userId, String keyword , String sectionName);
}
