package com.example.portfolio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.portfolio.entity.FailedPasswordReissue;
import com.example.portfolio.entity.PasswordReissueInfo;


@Repository
public interface FailedPasswordReissueRepository extends JpaRepository<FailedPasswordReissue, String> {
	 

    int countByToken(@Param("token") String token); // (1)

    //int create(FailedPasswordReissue event); // (2)

    void deleteByToken(@Param("token") String token); // (3)


}
