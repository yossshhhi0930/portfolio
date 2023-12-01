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
import java.time.MonthDay;

import lombok.Data;

@Entity
@Table(name = "section")
@Data
public class Section extends AbstractEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name = "section_id_seq")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private Long userId;

	@Column(nullable = false)
	private String name;

	@Column
	private String description;

	@ManyToOne
	@JoinColumn(name = "userId", insertable = false, updatable = false)
	private User user;

//    @OneToMany(mappedBy = "crop", cascade = CascadeType.ALL)
//    private List<Plan> plans;

}
