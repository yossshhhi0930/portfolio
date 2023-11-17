package com.example.portfolio.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "failedPasswordReissue")
@Data
public class FailedPasswordReissue extends AbstractEntity{

	@Id
    @SequenceGenerator(name = "users_id_seq")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long Id;
	
	@Column
    private String token; // (1)

    private LocalDateTime attemptDate; // (2)

}
