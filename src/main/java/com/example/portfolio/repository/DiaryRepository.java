package com.example.portfolio.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.portfolio.entity.Diary;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {
	Optional<Diary> findById(Long Id);

	List<Diary> findAllByUserIdOrderByUpdatedAtAsc(Long userId);

	List<Diary> findAllByPlanIdOrderByUpdatedAtAsc(Long planId);
}
