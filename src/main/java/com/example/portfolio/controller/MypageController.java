package com.example.portfolio.controller;

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

@Controller
public class MypageController {
	@Autowired
	PlanRepository planRepository;

	@Autowired
	DiaryRepository diaryRepository;

	@GetMapping(path = "/mypage")
	public String index(Principal principal, Model model) {
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		List<Diary> DiaryList = diaryRepository.findAllByUserIdOrderByUpdatedAtAsc(user.getUserId());
		List<Plan> list = planRepository.findAllByUserIdAndCompletionFalseOrderByUpdatedAtAsc(user.getUserId());
		List<Plan> doingList = new ArrayList<>();
		List<Plan> toDoList = new ArrayList<>();
		for (Plan plan : list) {
			if (plan.getSowing_date().isBefore(LocalDate.now())) {
				doingList.add(plan);
			}
			if (plan.getSowing_date().isAfter(LocalDate.now().minusDays(1))) {
				toDoList.add(plan);
			}
		}
		model.addAttribute("DiaryList", DiaryList);
		model.addAttribute("doingList", doingList);
		model.addAttribute("toDoList", toDoList);
		return "mypages/index";
	}

}
