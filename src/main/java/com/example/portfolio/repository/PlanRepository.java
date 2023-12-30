package com.example.portfolio.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.portfolio.entity.Plan;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {
	List<Plan> findAllByUserIdAndCompletionFalseOrderByUpdatedAtAsc(Long userId);

	List<Plan> findAllByUserIdAndCompletionTrueOrderByUpdatedAtAsc(Long userId);

	List<Plan> findAllByUserIdOrderByUpdatedAtAsc(Long userId);

	List<Plan> findAllByUserIdAndCompletionFalseAndCropNameContainingOrderByUpdatedAtAsc(Long userId, String keyword);

	List<Plan> findAllByUserIdAndCompletionTrueAndCropNameContainingOrderByUpdatedAtAsc(Long userId, String keyword);

	List<Plan> findAllByUserIdAndCropNameContainingOrderByUpdatedAtAsc(Long userId, String keyword);

	List<Plan> findAllByUserIdAndCompletionFalseAndSectionNameOrderByUpdatedAtAsc(Long userId, String sectionName);

	List<Plan> findAllByUserIdAndCompletionTrueAndSectionNameOrderByUpdatedAtAsc(Long userId, String sectionName);

	List<Plan> findAllByUserIdAndSectionNameOrderByUpdatedAtAsc(Long userId, String sectionName);

	List<Plan> findAllByUserIdAndCompletionFalseAndCropNameContainingAndSectionNameOrderByUpdatedAtAsc(Long userId,
			String keyword, String sectionName);

	List<Plan> findAllByUserIdAndCompletionTrueAndCropNameContainingAndSectionNameOrderByUpdatedAtAsc(Long userId,
			String keyword, String sectionName);

	List<Plan> findAllByUserIdAndCropNameContainingAndSectionNameOrderByUpdatedAtAsc(Long userId, String keyword,
			String sectionName);
}
