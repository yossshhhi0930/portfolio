package com.example.portfolio.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.portfolio.entity.CropImage;

@Repository
public interface CropImageReposiory extends JpaRepository<CropImage, Long> {
	Optional<CropImage> findById(Long id);

	List<CropImage> findByCropIdAndTopImageTrue(Long cropId);

	List<CropImage> findAllByCropIdOrderByUpdatedAtAsc(Long cropId);
}
