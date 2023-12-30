package com.example.portfolio.entity;

import java.io.Serializable;
import java.nio.file.Path;
import java.time.LocalDate;

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
@Table(name = "diary_image")
@Data
public class DiaryImage extends AbstractEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	public DiaryImage() {
		super();
	}

	public DiaryImage(Long diaryId, String path) {
		this.diaryId = diaryId;
		this.path = path;
	}

	@Id
	@SequenceGenerator(name = "diary_image_id_seq")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long diaryId;

	@Column(nullable = false)
	private String path;

	@ManyToOne
	@JoinColumn(name = "diaryId", insertable = false, updatable = false)
	private Diary diary;
}
