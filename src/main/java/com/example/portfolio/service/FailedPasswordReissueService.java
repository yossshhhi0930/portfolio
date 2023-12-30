package com.example.portfolio.service;

import org.springframework.stereotype.Service;

@Service
public interface FailedPasswordReissueService {
	public void cleanupEntities();
}
