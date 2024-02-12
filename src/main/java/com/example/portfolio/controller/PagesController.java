package com.example.portfolio.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * ページ遷移を担当するコントローラクラス
 */
@Controller
public class PagesController {

	/**
	 * トップ画面の表示
	 *
	 * @return トップ画面
	 */
	@RequestMapping("/")
	public String index() {
		// トップ画面を返す
		return "pages/index";
	}
}
