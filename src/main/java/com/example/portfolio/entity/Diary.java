package com.example.portfolio.entity;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import com.example.portfolio.entity.User;
import com.example.portfolio.entity.AbstractEntity;

import java.time.LocalDate;
import java.time.MonthDay;

import lombok.Data;

@Entity
@Table(name = "diary")
@Data
public class Diary extends AbstractEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	public Diary() {
		super();
	}

	public Diary(Long userId, Long planId, LocalDate record_date, String description) {
		this.userId = userId;
		this.planId = planId;
		this.record_date = record_date;
		this.description = description;
	}
	
	@Id
	@SequenceGenerator(name = "diary_id_seq")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long userId;
	
	@Column(nullable = false)
	private Long planId;
	
	@Column(nullable = false)
	private LocalDate record_date;

	@Column
	private String description;
	
	@ManyToOne
	@JoinColumn(name = "userId", insertable = false, updatable = false)
	private User user;

	@OneToMany(mappedBy = "diary", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
	private List<DiaryImage> diaryImages;

	@ManyToOne
	@JoinColumn(name = "planId", insertable = false, updatable = false)
	private Plan plan;
	
}
