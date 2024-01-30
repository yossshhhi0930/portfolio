package com.example.portfolio.controller;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.example.portfolio.entity.Diary;
import com.example.portfolio.entity.Plan;
import com.example.portfolio.entity.UserInf;
import com.example.portfolio.repository.DiaryRepository;
import com.example.portfolio.repository.PlanRepository;

/**
 * マイページに関する操作を担当するコントローラークラス
 */
@Controller
public class MypageController {

	// 栽培計画リポジトリの注入
	@Autowired
	PlanRepository planRepository;

	// 栽培日誌リポジトリの注入
	@Autowired
	DiaryRepository diaryRepository;

	/**
	 * マイページの表示<br>
	 * 栽培日誌リスト,現在栽培中の栽培計画リスト,栽培予定の栽培計画リストの生成、表示を行う
	 *
	 * @param principal 認証情報
	 * @param model     ビューで使用するモデル
	 * @return マイページ
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	@GetMapping(path = "/mypage")
	public String index(Principal principal, Model model) throws IOException {
		// 認証情報からユーザーを取得
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		// 取得したユーザーのユーザーIDから、ユーザーに対応する全ての栽培日誌を取得し、栽培日誌リストを生成
		List<Diary> DiaryList = diaryRepository.findAllByUserIdOrderByUpdatedAtAsc(user.getUserId());
		// 取得したユーザーのユーザーIDから、ユーザーに対応する全ての栽培計画を取得し、栽培計画リストを生成
		List<Plan> list = planRepository.findAllByUserIdAndCompletionFalseOrderByUpdatedAtAsc(user.getUserId());
		// 現在栽培中の栽培計画リストを初期化
		List<Plan> doingList = new ArrayList<>();
		// 栽培予定の栽培計画リストを初期化
		List<Plan> toDoList = new ArrayList<>();
		// 生成した栽培計画リストの全ての栽培計画に対して行う処理
		for (Plan plan : list) {
			// 栽培計画の播種日が今日より前の日付であれば、現在栽培中の栽培計画リストに追加
			if (plan.getSowing_date().isBefore(LocalDate.now())) {
				doingList.add(plan);
			}
			// 栽培計画の播種日が今日より後（今日を含める）の日付であれば、栽培予定の栽培計画リストに追加
			if (plan.getSowing_date().isAfter(LocalDate.now().minusDays(1))) {
				toDoList.add(plan);
			}
		}
		// 栽培日誌リストをモデルに設定
		model.addAttribute("DiaryList", DiaryList);
		// 現在栽培中の栽培計画リストをモデルに設定
		model.addAttribute("doingList", doingList);
		// 栽培予定の栽培計画リストをモデルに設定
		model.addAttribute("toDoList", toDoList);
		// マイページの画面を返す
		return "mypages/index";
	}

}
