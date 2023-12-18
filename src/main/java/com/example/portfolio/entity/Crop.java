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
import java.time.MonthDay;

import lombok.Data;

@Entity
@Table(name = "crop")
@Data
public class Crop extends AbstractEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name = "crop_id_seq")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long userId;

	@Column(nullable = false, unique = true)
	private String name;

	@Column(nullable = false)
	private MonthDay sowing_start;

	@Column(nullable = false)
	private MonthDay sowing_end;

	@Column(nullable = false)
	private int cultivationp_period;
	
	@Column
	private String manual;

	@ManyToOne
	@JoinColumn(name = "userId", insertable = false, updatable = false)
	private User user;

	@OneToMany(mappedBy = "crop", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
	private List<CropImage> cropImages;

	@OneToMany(mappedBy = "crop", fetch = FetchType.LAZY)
	private List<Plan> plans;
	
}
