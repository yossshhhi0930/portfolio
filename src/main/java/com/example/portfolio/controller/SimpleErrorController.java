//package com.example.portfolio.controller;
//
//import javax.servlet.http.HttpServletRequest;
//
//import org.springframework.boot.web.servlet.error.ErrorController;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.servlet.ModelAndView;
//
//@Controller
//@RequestMapping("${server.error.path:${error.path:/error}}") // エラーページへのマッピング
//public class SimpleErrorController implements ErrorController {
//
//  /**
//   * エラーページのパスを返す。
//   *
//   * @return エラーページのパス
//   */
//	
//	public String getErrorPath() {
//    return "/error";
//  }
//
//  /**
//   * レスポンス用の ModelAndView オブジェクトを返す。
//   *
//   * @param req リクエスト情報
//   * @param mav レスポンス情報
//   * @return HTML レスポンス用の ModelAndView オブジェクト
//   */
//  @RequestMapping
//  public ModelAndView error(HttpServletRequest req, ModelAndView mav) {
//
//    // どのエラーでも 404 Not Found にする
//    // 必要に応じてステータコードや出力内容をカスタマイズ可能
//    mav.setStatus(HttpStatus.NOT_FOUND);
//
//    // ビュー名を指定する
//    // Thymeleaf テンプレート src/main/resources/templates/error.html を使用
//    mav.setViewName("error");
//
//    return mav;
//  }
//}
