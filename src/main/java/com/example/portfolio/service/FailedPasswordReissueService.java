package com.example.portfolio.service;

import org.springframework.stereotype.Service;

/**
 * パスワード再発行失敗サービスクラス
 */
@Service
public interface FailedPasswordReissueService {
	public void cleanupEntities();
}
