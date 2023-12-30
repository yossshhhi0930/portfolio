package com.example.portfolio.entity;

import java.io.Serializable;
import java.util.List;
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

import lombok.Data;

@Entity
@Table(name = "section")
@Data
public class Section extends AbstractEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	public Section() {
		super();
	}

	public Section(Long userId, String name, String description) {
		this.userId = userId;
		this.name = name;
		this.description = description;
	}

	@Id
	@SequenceGenerator(name = "section_id_seq")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long userId;

	@Column(nullable = false)
	private String name;

	@Column
	private String description;

	@ManyToOne
	@JoinColumn(name = "userId", insertable = false, updatable = false)
	private User user;

	@OneToMany(mappedBy = "section", fetch = FetchType.LAZY)
	private List<Plan> plans;

}
