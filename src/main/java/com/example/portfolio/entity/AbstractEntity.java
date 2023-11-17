package com.example.portfolio.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import lombok.Data;

@MappedSuperclass
@Data
public class AbstractEntity {

	@Column(name = "created_at")
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updatedAt;

    @PrePersist
    public void onPrePersist() {
        Date date = new Date();
        setCreatedAt(date);
        setUpdatedAt(date);
    }

    @PreUpdate
    public void onPreUpdate() {
        setUpdatedAt(new Date());
    }
    
    
	public User getLoginUser() {
		// TODO Auto-generated method stub
		return null;
	}
}
