package com.example.portfolio.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.portfolio.entity.Crop;

@Repository
public interface CropRepository extends JpaRepository<Crop, Long> {
	List<Crop> findAllByUserIdOrderByUpdatedAtAsc(Long userId);

	Crop findByNameAndUserId(String name, Long userId);

	Optional<Crop> findById(Long Id);

	List<Crop> findAllByNameContainingAndUserId(String keyword, Long userId);
}
