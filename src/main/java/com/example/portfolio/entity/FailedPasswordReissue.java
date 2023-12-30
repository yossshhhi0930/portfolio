package com.example.portfolio.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "failed_password")
@Data
public class FailedPasswordReissue extends AbstractEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	public FailedPasswordReissue() {
		super();
	}

	public FailedPasswordReissue(String email) {
		this.email = email;
	}

	@Id
	@SequenceGenerator(name = "failedPassword_id_seq")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long Id;

	@Column(nullable = false)
	private String email;

}
