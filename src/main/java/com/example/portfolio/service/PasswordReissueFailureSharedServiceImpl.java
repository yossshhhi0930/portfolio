package com.example.portfolio.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.portfolio.entity.FailedPasswordReissue;
import com.example.portfolio.repository.FailedPasswordReissueRepository;

@Service
public class PasswordReissueFailureSharedServiceImpl implements
        PasswordReissueFailureSharedService {

	@Autowired
    FailedPasswordReissueRepository failedPasswordReissueRepository;

    @Override
    public void resetFailure(String token) {
        FailedPasswordReissue event = new FailedPasswordReissue(); // (2)
        event.setToken(token);
        event.setAttemptDate(LocalDateTime.now());
        failedPasswordReissueRepository.save(event); // (3)
    }

}