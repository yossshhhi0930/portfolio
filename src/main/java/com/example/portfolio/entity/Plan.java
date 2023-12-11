package com.example.portfolio.entity;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
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
@Table(name = "plan")
@Data
public class Plan extends AbstractEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name = "plan_id_seq")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private Long userId;

	@Column
	private Long cropId;

	@Column
	private LocalDate sowing_date;

	@Column
	private LocalDate harvest_completion_date;

	@Column
	private Long sectionId;

	@Column
	private boolean completion;

	@ManyToOne
	@JoinColumn(name = "userId", insertable = false, updatable = false)
	private User user;

	@ManyToOne
	@JoinColumn(name = "cropId", insertable = false, updatable = false)
	private Crop crop;
	
	@ManyToOne
	@JoinColumn(name = "sectionId", insertable = false, updatable = false)
	private Section section;

}
