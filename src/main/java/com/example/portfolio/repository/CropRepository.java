package com.example.portfolio.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.portfolio.entity.Crop;

@Repository
public interface CropRepository extends JpaRepository<Crop, Long> {
	Iterable<Crop>findAllByUserIdOrderByUpdatedAtDesc(Long userId);
	Crop findByName(String name);
	Optional<Crop> findById(Long Id);
	List<Crop>findByNameContaining(String keyword);
	void saveAndFlush(Optional<Crop> entity);
}
