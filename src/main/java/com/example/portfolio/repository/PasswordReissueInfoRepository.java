package com.example.portfolio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.portfolio.entity.PasswordReissueInfo;

@Repository
public interface PasswordReissueInfoRepository extends JpaRepository<PasswordReissueInfo, String> {
 
 //PasswordReissueInfo findOne(@Param("token") String token); // (2)元の文
	PasswordReissueInfo findByToken(@Param("token")String token);
	PasswordReissueInfo findBySecret(String secret);
 //int delete(@Param("token") String token); // (3)元の文

}
