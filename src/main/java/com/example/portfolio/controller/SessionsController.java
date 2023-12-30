package com.example.portfolio.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SessionsController {

	// ログイン画面の表示
	@GetMapping(path = "/login")
	public String index() {
		return "/sessions/new";
	}

	// ログインに失敗した場合にエラーを返す
	@GetMapping(path = "/login-failure")
	public String loginFailure(Model model) {
		model.addAttribute("hasMessage", true);
		model.addAttribute("class", "alert-danger");
		model.addAttribute("message", "メールアドレスまたはパスワードに誤りがあります。");
		return "/sessions/new";
	}

	// ログアウト完了
	@GetMapping(path = "/logout-complete")
	public String logoutComplete(Model model) {
		model.addAttribute("hasMessage", true);
		model.addAttribute("class", "alert-info");
		model.addAttribute("message", "ログアウトしました。");
		return "/layouts/complete";
	}
}