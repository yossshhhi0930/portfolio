package com.example.portfolio.entity;

import java.io.Serializable;
import java.nio.file.Path;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import com.example.portfolio.entity.AbstractEntity;

import lombok.Data;

@Entity
@Table(name = "crop_image")
@Data
public class CropImage extends AbstractEntity implements Serializable {
    private static final long serialVersionUID = 1L; 

	@Id
    @SequenceGenerator(name = "crop_image_id_seq")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	@Column(nullable = false)
	private Long cropId;
	
	@Column(nullable = false)
	private String path;
	
	@ManyToOne
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    private Crop crop;
}
