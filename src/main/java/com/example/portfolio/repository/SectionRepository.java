package com.example.portfolio.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.portfolio.entity.Crop;
import com.example.portfolio.entity.Section;

@Repository
public interface SectionRepository extends JpaRepository<Section, Long> {
	Section findByName(String name);
	
	Optional<Section> findById(Long id);
	List<Section>findAllByUserIdOrderByUpdatedAtDesc(Long userId);
	List<Section>findByNameContaining(String keyword);
	
}
