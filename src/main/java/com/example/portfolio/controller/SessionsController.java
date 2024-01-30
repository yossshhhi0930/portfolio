package com.example.portfolio.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * セッションに関する操作（ログイン、ログアウト）を担当するコントローラークラス
 */
@Controller
public class SessionsController {

	/**
	 * ログイン画面を表示
	 *
	 * @return ログイン画面
	 */
	@GetMapping(path = "/login")
	public String index() {
		return "/sessions/new";
	}

	/**
	 * ログイン失敗時の処理
	 *
	 * @param model ビューで使用するモデル
	 * @return ログイン画面
	 */
	@GetMapping(path = "/login-failure")
	public String loginFailure(Model model) {
		// アラートメッセージをモデルに設定
		model.addAttribute("hasMessage", true);
		model.addAttribute("class", "alert-danger");
		model.addAttribute("message", "メールアドレスまたはパスワードに誤りがあります。");
		// ログイン画面を返す
		return "/sessions/new";
	}

	/**
	 * ログアウト完了画面を表示
	 *
	 * @param model ビューで使用するモデル
	 * @return ログアウト完了画面
	 */
	@GetMapping(path = "/logout-complete")
	public String logoutComplete(Model model) {
		// アラートメッセージをモデルに設定
		model.addAttribute("hasMessage", true);
		model.addAttribute("class", "alert-info");
		model.addAttribute("message", "ログアウトしました。");
		// ログアウト完了画面を返す
		return "/layouts/complete";
	}
}