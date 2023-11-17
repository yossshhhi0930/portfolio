package com.example.portfolio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.portfolio.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
	User findByUserId(Long userId);
    User findByUsername(String username);
    
//    @Query("SELECT c FROM users c WHERE c.verificationCode = ?1")
    User  findByVerificationCode(String verificationCode);
    
//    @Query("UPDATE users c SET  c.verificationCode = null,c.enabled= true  WHERE c.id = ?1")
//    @Modifying(clearAutomatically = true)
//    public void enabled(Long userId);
}
