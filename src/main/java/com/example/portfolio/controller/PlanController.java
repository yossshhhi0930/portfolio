package com.example.portfolio.controller;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import com.example.portfolio.repository.CropRepository;
import com.example.portfolio.repository.DiaryRepository;
import com.example.portfolio.repository.PlanRepository;
import com.example.portfolio.repository.SectionRepository;
import com.example.portfolio.entity.Crop;
import com.example.portfolio.entity.Diary;
import com.example.portfolio.entity.Plan;
import com.example.portfolio.entity.Section;
import com.example.portfolio.entity.UserInf;
import com.example.portfolio.form.PlanForm;

@Controller
public class PlanController {

	protected static Logger log = LoggerFactory.getLogger(PlanController.class);

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	CropRepository cropRepository;

	@Autowired
	SectionRepository sectionRepository;

	@Autowired
	PlanRepository repository;

	@Autowired
	DiaryRepository diaryRepository;

	// 栽培計画登録画面表示
	@GetMapping(path = "/plans/new")
	public String newPlan(@RequestParam(required = false) Long cropId, Model model) {
		PlanForm form = new PlanForm();
		// 作物の情報があれば、作物名をformにセット
		if (cropId != null) {
			Optional<Crop> optionalCrop = cropRepository.findById(cropId);
			Crop crop = optionalCrop.orElseThrow(() -> new RuntimeException("Crop not found"));
			form.setCropName(crop.getName());
		}
		model.addAttribute("form", form);
		return "plans/new";
	}

	// 作物検索、収穫完了予定日取得、使用可能な区画取得、栽培計画登録
	@PostMapping(path = "/plan")
	public String create(Principal principal, @RequestParam(name = "cmd") String cmd,
			@Validated @ModelAttribute("form") PlanForm form, BindingResult result, Model model,
			RedirectAttributes attributes) throws IOException {
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		Long userId = user.getUserId();
		String cropName = form.getCropName();
		String sectionName = form.getSectionName();
		LocalDate sowing_date = form.getSowing_date();
		LocalDate harvest_completion_date = form.getHarvest_completion_date();
		boolean completion = form.isCompletion();
		Section section = sectionRepository.findByNameAndUserId(sectionName, userId);
		Crop crop = cropRepository.findByNameAndUserId(cropName, userId);
		List<Section> searchSections = new ArrayList<>();
		// sowing_dateとharvest_completion_dateに値が入力されていれば、利用可能な区画を取得
		if (sowing_date != null && harvest_completion_date != null) {
			searchSections = availableSctionGenerater(user.getUserId(), sowing_date, harvest_completion_date, null);
		}
		// 作物検索
		if ("search".equals(cmd)) {
			List<Crop> searchCrops = new ArrayList<>();
			if (cropName == null || cropName.isEmpty()) {
				searchCrops = cropRepository.findAllByUserIdOrderByUpdatedAtAsc(userId);
			} else {
				searchCrops = cropRepository.findAllByNameContainingAndUserId(cropName, userId);
			}
			if (searchCrops.isEmpty()) {
				model.addAttribute("searchCropsMessage", "その作物は見つかりませんでした。");
			}
			model.addAttribute("searchCrops", searchCrops);
			model.addAttribute("searchSections", searchSections);
			return "plans/new";
		}
		// 作物名と播種日から収穫完了予定日を取得
		if ("calculate".equals(cmd)) {
			// 作物名が入力されていない場合に返すエラーの追加
			if (cropName == null || cropName.isEmpty()) {
				FieldError fieldError = new FieldError(result.getObjectName(), "cropName", "作物名を入力してください。");
				result.addError(fieldError);
				// 入力された作物名が登録されてない場合に返すエラーの追加
			} else if (crop == null) {
				FieldError fieldError = new FieldError(result.getObjectName(), "cropName", "作物名に誤りがあります。");
				result.addError(fieldError);
			}
			// 播種日が入力されていない場合に返すエラーの追加
			if (sowing_date == null) {
				FieldError fieldError = new FieldError(result.getObjectName(), "sowing_date", "播種日を入力してください。");
				result.addError(fieldError);
			}
			if (result.hasErrors()) {
				model.addAttribute("searchSections", searchSections);
				model.addAttribute("hasMessage", true);
				model.addAttribute("class", "alert-danger");
				model.addAttribute("message", "収穫完了予定日の算出に失敗しました。");
				return "plans/new";
			}
			// 収穫完了予定日を算出し、formにセット
			int cultivationp_period = crop.getCultivationp_period();
			LocalDate newHarvest_completion_date = sowing_date.plusDays(cultivationp_period);
			form.setHarvest_completion_date(newHarvest_completion_date);
			// 利用可能な区画を更新
			searchSections = availableSctionGenerater(userId, sowing_date, newHarvest_completion_date, null);
			model.addAttribute("searchSections", searchSections);
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-info");
			model.addAttribute("message", "収穫完了予定日の算出に成功しました。");
			return "plans/new";
		}
		// 利用可能な区画の取得
		if ("select".equals(cmd)) {
			// 播種日が入力されていない場合に返すエラーの追加
			if (sowing_date == null) {
				FieldError fieldError = new FieldError(result.getObjectName(), "sowing_date", "播種日を入力してください。");
				result.addError(fieldError);
				// 収穫完了予定日が入力されていない場合に返すエラーの追加
			} else if (harvest_completion_date == null) {
				FieldError fieldError = new FieldError(result.getObjectName(), "harvest_completion_date",
						"収穫完了予定日を入力して下さい。");
				result.addError(fieldError);
				// 収穫完了日に播種日より前の日付が入力されている場合に返すエラーの追加
			} else if (harvest_completion_date.isBefore(sowing_date)) {
				FieldError fieldError = new FieldError(result.getObjectName(), "harvest_completion_date",
						"播種日より後の日付を入力したください。");
				result.addError(fieldError);
			}
			if (result.hasErrors()) {
				model.addAttribute("searchSections", searchSections);
				model.addAttribute("hasMessage", true);
				model.addAttribute("class", "alert-danger");
				model.addAttribute("message", "利用可能な区画の取得に失敗しました。");
				return "plans/new";
			}
			if (searchSections.isEmpty()) {
				model.addAttribute("searchSectionsMessage", "利用可能な区画はありません。");
			}
			model.addAttribute("searchSections", searchSections);
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-info");
			model.addAttribute("message", "利用可能な区画の取得に成功しました。");
			return "plans/new";
		}
		// 栽培計画登録
		if ("register".equals(cmd)) {
			// 作物名が入力されていない場合に返すエラーの追加
			if (cropName == null || cropName.isEmpty()) {
				FieldError fieldError = new FieldError(result.getObjectName(), "cropName", "作物名を入力してください。");
				result.addError(fieldError);
				// 入力された作物名が登録されてない場合に返すエラーの追加
			} else if (crop == null) {
				FieldError fieldError = new FieldError(result.getObjectName(), "cropName", "作物名に誤りがあります。");
				result.addError(fieldError);
			}
			// 播種日が入力されていない場合に返すエラーの追加
			if (sowing_date == null) {
				FieldError fieldError = new FieldError(result.getObjectName(), "sowing_date", "播種日を入力してください。");
				result.addError(fieldError);
				// 収穫完了予定日が入力されていない場合に返すエラーの追加
			} else if (harvest_completion_date == null) {
				FieldError fieldError = new FieldError(result.getObjectName(), "harvest_completion_date",
						"収穫完了予定日を入力して下さい。");
				result.addError(fieldError);
				// 収穫完了日に播種日より前の日付が入力されている場合に返すエラーの追加
			} else if (harvest_completion_date.isBefore(sowing_date)) {
				FieldError fieldError = new FieldError(result.getObjectName(), "harvest_completion_date",
						"播種日より後の日付を入力したください。");
				result.addError(fieldError);
				// 区画が入力されていない場合に返すエラーの追加
			} else if (sectionName == null || sectionName.isEmpty()) {
				FieldError fieldError = new FieldError(result.getObjectName(), "sectionName", "区画を選択して下さい。");
				result.addError(fieldError);
				// 入力された区画名が登録されてない場合に返すエラーの追加
			} else if (section == null) {
				FieldError fieldError = new FieldError(result.getObjectName(), "sectionName", "区画名に誤りがあります。");
				result.addError(fieldError);
				// 入力された区画が利用不可の場合に返すエラーの追加
			} else if (!searchSections.contains(section)) {
				FieldError fieldError = new FieldError(result.getObjectName(), "sectionName", "この区画は使用不可です。");
				result.addError(fieldError);
			}
			if (result.hasErrors()) {
				model.addAttribute("searchSections", searchSections);
				model.addAttribute("hasMessage", true);
				model.addAttribute("class", "alert-danger");
				model.addAttribute("message", "作物栽培計画の登録に失敗しました。");
				return "plans/new";
			}
			Plan entity = new Plan(userId, crop.getId(), section.getId(), sowing_date, harvest_completion_date,
					completion);
			repository.saveAndFlush(entity);
			attributes.addFlashAttribute("hasMessage", true);
			attributes.addFlashAttribute("class", "alert-info");
			attributes.addFlashAttribute("message", "栽培計画の登録が完了しました。");
			return "redirect:/plans/detail/" + entity.getId();
		}
		model.addAttribute("searchSections", searchSections);
		model.addAttribute("hasMessage", true);
		model.addAttribute("class", "alert-danger");
		model.addAttribute("message", "作物栽培計画の登録に失敗しました。");
		return "plans/new";
	}

	// 栽培計画詳細表示
	@GetMapping(path = "/plans/detail/{planId}")
	public String showDetail(@PathVariable Long planId, Model model) throws IOException {
		Optional<Plan> optionalPlan = repository.findById(planId);
		Plan entity = optionalPlan.orElseThrow(() -> new RuntimeException("Plan not found"));
		List<Diary> list = diaryRepository.findAllByPlanIdOrderByUpdatedAtAsc(planId);
		model.addAttribute("list", list);
		model.addAttribute("plan", entity);
		return "plans/detail";
	}

	// 編集画面表示
	@GetMapping(path = "/plans/edit/{planId}")
	public String showEditPage(@PathVariable Long planId, Model model, Principal principal) throws IOException {
		Optional<Plan> optionalPlan = repository.findById(planId);
		Plan entity = optionalPlan.orElseThrow(() -> new RuntimeException("Plan not found"));
		PlanForm form = getPlanForm(entity);
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		List<Section> searchSections = availableSctionGenerater(user.getUserId(), form.getSowing_date(),
				form.getHarvest_completion_date(), form.getId());
		model.addAttribute("searchSections", searchSections);
		model.addAttribute("form", form);
		return "plans/edit";
	}

	// 作物検索、収穫完了予定日取得、使用可能な区画取得、栽培計画編集
	@PostMapping(path = "/plans/edit-complete")
	public String edit(Principal principal, @RequestParam(name = "cmd") String cmd,
			@Validated @ModelAttribute("form") PlanForm form, BindingResult result, Model model,
			RedirectAttributes attributes) throws IOException {
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		Long userId = user.getUserId();
		Long id = form.getId();
		String cropName = form.getCropName();
		String sectionName = form.getSectionName();
		LocalDate sowing_date = form.getSowing_date();
		LocalDate harvest_completion_date = form.getHarvest_completion_date();
		boolean completion = form.isCompletion();
		Crop crop = cropRepository.findByNameAndUserId(cropName, userId);
		Section section = sectionRepository.findByNameAndUserId(sectionName, userId);
		List<Section> searchSections = new ArrayList<>();
		// sowing_dateとharvest_completion_dateに値が入力されていれば、利用可能な区画を取得
		if (sowing_date != null && harvest_completion_date != null) {
			searchSections = availableSctionGenerater(userId, sowing_date, harvest_completion_date, id);
		}
		// 作物検索
		if ("search".equals(cmd)) {
			List<Crop> searchCrops = new ArrayList<>();
			if (cropName == null || cropName.isEmpty()) {
				searchCrops = cropRepository.findAllByUserIdOrderByUpdatedAtAsc(userId);
			} else {
				searchCrops = cropRepository.findAllByNameContainingAndUserId(cropName, userId);
			}
			if (searchCrops.isEmpty()) {
				model.addAttribute("searchCropsMessage", "その作物は見つかりませんでした。");
			}
			model.addAttribute("searchCrops", searchCrops);
			model.addAttribute("searchSections", searchSections);
			return "plans/edit";
		}
		// 作物名と播種日から収穫完了予定日を取得
		if ("calculate".equals(cmd)) {
			// 作物名が入力されていない場合に返すエラーの追加
			if (cropName == null || cropName.isEmpty()) {
				FieldError fieldError = new FieldError(result.getObjectName(), "cropName", "作物名を入力してください。");
				result.addError(fieldError);
				// 入力された作物名が登録されてない場合に返すエラーの追加
			} else if (crop == null) {
				FieldError fieldError = new FieldError(result.getObjectName(), "cropName", "作物名に誤りがあります。");
				result.addError(fieldError);
			}
			// 播種日が入力されていない場合に返すエラーの追加
			if (sowing_date == null) {
				FieldError fieldError = new FieldError(result.getObjectName(), "sowing_date", "播種日を入力してください。");
				result.addError(fieldError);
			}
			if (result.hasErrors()) {
				model.addAttribute("searchSections", searchSections);
				model.addAttribute("hasMessage", true);
				model.addAttribute("class", "alert-danger");
				model.addAttribute("message", "収穫完了予定日の算出に失敗しました。");
				return "plans/edit";
			}
			// 収穫完了予定日を算出し、formにセット
			int cultivationp_period = crop.getCultivationp_period();
			LocalDate newHarvest_completion_date = sowing_date.plusDays(cultivationp_period);
			form.setHarvest_completion_date(newHarvest_completion_date);
			// 利用可能な区画を更新
			searchSections = availableSctionGenerater(userId, sowing_date, newHarvest_completion_date, id);
			model.addAttribute("searchSections", searchSections);
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-info");
			model.addAttribute("message", "収穫完了予定日の算出に成功しました。");
			return "plans/edit";
		}
		// 利用可能な区画の取得
		if ("select".equals(cmd)) {
			// 播種日が入力されていない場合に返すエラーの追加
			if (sowing_date == null) {
				FieldError fieldError = new FieldError(result.getObjectName(), "sowing_date", "播種日を入力してください。");
				result.addError(fieldError);
				// 収穫完了予定日が入力されていない場合に返すエラーの追加
			} else if (harvest_completion_date == null) {
				FieldError fieldError = new FieldError(result.getObjectName(), "harvest_completion_date",
						"収穫完了予定日を入力して下さい。");
				result.addError(fieldError);
				// 収穫完了日に播種日より前の日付が入力されている場合に返すエラーの追加
			} else if (harvest_completion_date.isBefore(sowing_date)) {
				FieldError fieldError = new FieldError(result.getObjectName(), "harvest_completion_date",
						"播種日より後の日付を入力したください。");
				result.addError(fieldError);
			}
			if (result.hasErrors()) {
				model.addAttribute("searchSections", searchSections);
				model.addAttribute("hasMessage", true);
				model.addAttribute("class", "alert-danger");
				model.addAttribute("message", "利用可能な区画の取得に失敗しました。");
				return "plans/edit";
			}
			if (searchSections.isEmpty()) {
				model.addAttribute("searchSectionsMessage", "利用可能な区画はありません。");
			}
			model.addAttribute("searchSections", searchSections);
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-info");
			model.addAttribute("message", "利用可能な区画の取得に成功しました。");
			return "plans/edit";
		}

		// 栽培計画編集
		if ("register".equals(cmd)) {
			// 作物名が入力されていない場合に返すエラーの追加
			if (cropName == null || cropName.isEmpty()) {
				FieldError fieldError = new FieldError(result.getObjectName(), "cropName", "作物名を入力してください。");
				result.addError(fieldError);
				// 入力された作物名が登録されてない場合に返すエラーの追加
			} else if (crop == null) {
				FieldError fieldError = new FieldError(result.getObjectName(), "cropName", "作物名に誤りがあります。");
				result.addError(fieldError);
			}
			// 播種日が入力されていない場合に返すエラーの追加
			if (sowing_date == null) {
				FieldError fieldError = new FieldError(result.getObjectName(), "sowing_date", "播種日を入力してください。");
				result.addError(fieldError);
				// 収穫完了予定日が入力されていない場合に返すエラーの追加
			} else if (harvest_completion_date == null) {
				FieldError fieldError = new FieldError(result.getObjectName(), "harvest_completion_date",
						"収穫完了予定日を入力して下さい。");
				result.addError(fieldError);
				// 収穫完了日に播種日より前の日付が入力されている場合に返すエラーの追加
			} else if (harvest_completion_date.isBefore(sowing_date)) {
				FieldError fieldError = new FieldError(result.getObjectName(), "harvest_completion_date",
						"播種日より後の日付を入力したください。");
				result.addError(fieldError);
				// 区画が入力されていない場合に返すエラーの追加
			} else if (sectionName == null || sectionName.isEmpty()) {
				FieldError fieldError = new FieldError(result.getObjectName(), "sectionName", "区画を選択して下さい。");
				result.addError(fieldError);
				// 入力された区画名が登録されてない場合に返すエラーの追加
			} else if (section == null) {
				FieldError fieldError = new FieldError(result.getObjectName(), "sectionName", "区画名に誤りがあります。");
				result.addError(fieldError);
				// 入力された区画が利用不可の場合に返すエラーの追加
			} else if (!searchSections.contains(section)) {
				FieldError fieldError = new FieldError(result.getObjectName(), "sectionName", "この区画は使用不可です。");
				result.addError(fieldError);
			}
			if (result.hasErrors()) {
				model.addAttribute("searchSections", searchSections);
				model.addAttribute("hasMessage", true);
				model.addAttribute("class", "alert-danger");
				model.addAttribute("message", "作物栽培計画の編集に失敗しました。");
				return "plans/edit";
			}
			Optional<Plan> optionalPlan = repository.findById(id);
			Plan entity = optionalPlan.orElseThrow(() -> new RuntimeException("Plan not found"));
			entity.setCropId(crop.getId());
			entity.setSowing_date(sowing_date);
			entity.setHarvest_completion_date(harvest_completion_date);
			entity.setSectionId(section.getId());
			entity.setCompletion(completion);
			repository.saveAndFlush(entity);
			attributes.addFlashAttribute("hasMessage", true);
			attributes.addFlashAttribute("class", "alert-info");
			attributes.addFlashAttribute("message", "栽培計画の編集に成功しました。");
			return "redirect:/plans/detail/" + entity.getId();
		}
		model.addAttribute("searchSections", searchSections);
		model.addAttribute("hasMessage", true);
		model.addAttribute("class", "alert-danger");
		model.addAttribute("message", "栽培計画の編集に失敗しました。");
		return "plans/edit";
	}

	// 栽培計画削除
	@GetMapping(path = "/plans/delete/{planId}")
	public String delete(@PathVariable Long planId, Model model, RedirectAttributes redirAttrs) throws IOException {
		repository.deleteById(planId);
		redirAttrs.addFlashAttribute("hasMessage", true);
		redirAttrs.addFlashAttribute("class", "alert-info");
		redirAttrs.addFlashAttribute("message", "作物データの削除に成功しました。");
		return "redirect:/plans/list";
	}

	// 栽培計画一覧表示
	@GetMapping(path = "/plans/list")
	public String showList(Principal principal, Model model) throws IOException {
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		// 計画中のplanのみ表示
		List<Plan> list = repository.findAllByUserIdAndCompletionFalseOrderByUpdatedAtAsc(user.getUserId());
		List<PlanForm> gantList = getGantList(null, list);
		Set<Integer> yearList = getYearList(user.getUserId());
		List<Section> sectionList = sectionRepository.findAllByUserIdOrderByUpdatedAtAsc(user.getUserId());
		model.addAttribute("list", list);
		model.addAttribute("gantList", gantList);
		model.addAttribute("yearList", yearList);
		model.addAttribute("sectionList", sectionList);
		model.addAttribute("option", "progress");
		return "/plans/list";
	}

	// 栽培計画検索
	@PostMapping(path = "/plans/search")
	public String searchCrops(Principal principal, @RequestParam(name = "option", required = false) String option,
			@RequestParam(name = "keyword", required = false) String keyword,
			@RequestParam(name = "sectionName", required = false) String sectionName,
			@RequestParam(name = "year", required = false) Integer year, Model model, RedirectAttributes redirAttrs)
			throws IOException {
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		List<Section> sectionList = sectionRepository.findAllByUserIdOrderByUpdatedAtAsc(user.getUserId());
		List<Plan> list = new ArrayList<>();
		if (sectionName == null || sectionName.isEmpty()) {
			if (keyword == null) {
				if ("progress".equals(option)) {
					list = repository.findAllByUserIdAndCompletionFalseOrderByUpdatedAtAsc(user.getUserId());
				} else if ("completion".equals(option)) {
					list = repository.findAllByUserIdAndCompletionTrueOrderByUpdatedAtAsc(user.getUserId());
				} else {
					list = repository.findAllByUserIdOrderByUpdatedAtAsc(user.getUserId());
				}
			}
			if (keyword != null) {
				if ("progress".equals(option)) {
					list = repository.findAllByUserIdAndCompletionFalseAndCropNameContainingOrderByUpdatedAtAsc(
							user.getUserId(), keyword);
				} else if ("completion".equals(option)) {
					list = repository.findAllByUserIdAndCompletionTrueAndCropNameContainingOrderByUpdatedAtAsc(
							user.getUserId(), keyword);
				} else {
					list = repository.findAllByUserIdAndCropNameContainingOrderByUpdatedAtAsc(user.getUserId(),
							keyword);
				}
			}
		} else if (sectionName != null) {
			if (keyword == null) {
				if ("progress".equals(option)) {
					list = repository.findAllByUserIdAndCompletionFalseAndSectionNameOrderByUpdatedAtAsc(
							user.getUserId(), sectionName);
				} else if ("completion".equals(option)) {
					list = repository.findAllByUserIdAndCompletionTrueAndSectionNameOrderByUpdatedAtAsc(
							user.getUserId(), sectionName);

				} else {
					list = repository.findAllByUserIdAndSectionNameOrderByUpdatedAtAsc(user.getUserId(), sectionName);
				}
			}
			if (keyword != null) {
				if ("progress".equals(option)) {
					list = repository
							.findAllByUserIdAndCompletionFalseAndCropNameContainingAndSectionNameOrderByUpdatedAtAsc(
									user.getUserId(), keyword, sectionName);
				} else if ("completion".equals(option)) {
					list = repository
							.findAllByUserIdAndCompletionTrueAndCropNameContainingAndSectionNameOrderByUpdatedAtAsc(
									user.getUserId(), keyword, sectionName);
				} else {
					list = repository.findAllByUserIdAndCropNameContainingAndSectionNameOrderByUpdatedAtAsc(
							user.getUserId(), keyword, sectionName);
				}
			}
		}
		if (year != null) {
			list = getListOfYear(year, list);
		}
		if (list.isEmpty()) {
			model.addAttribute("message", "その栽培計画は見つかりませんでした。");
		}
		Set<Integer> yearList = getYearList(user.getUserId());
		List<PlanForm> gantList = getGantList(year, list);
		model.addAttribute("list", list);
		model.addAttribute("gantList", gantList);
		model.addAttribute("yearList", yearList);
		model.addAttribute("sectionList", sectionList);
		model.addAttribute("sectionName", sectionName);
		model.addAttribute("keyword", keyword);
		model.addAttribute("option", option);
		model.addAttribute("year", year);
		return "/plans/list";
	}

	// 区画が使用可能な区画のリスト生成
	private List<Section> availableSctionGenerater(Long userId, LocalDate sowing_date,
			LocalDate harvest_completion_date, Long planId) throws IOException {
		List<Section> sectionList = sectionRepository.findAllByUserIdOrderByUpdatedAtAsc(userId);
		List<Section> searchSections = new ArrayList<>();
		outerLoop: for (Section section : sectionList) {
			List<Plan> planList = section.getPlans();
			// planの編集の際には、既に登録済の当plamを検証から除外
			if (planId != null) {
				Optional<Plan> optionalPlan = repository.findById(planId);
				Plan thisPlan = optionalPlan.orElseThrow(() -> new RuntimeException("Plan not found"));
				if (planList.contains(thisPlan)) {
					planList.remove(thisPlan);
				}
			}
			// secionが持つｐlanに期間が被るplanが他に一つでも存在していれば親拡張for文のループから脱出
			for (Plan plan : planList) {
				LocalDate otherPlanSowing_date = plan.getSowing_date();
				LocalDate otherPlanHarvest_completion_date = plan.getHarvest_completion_date();
				if (!(otherPlanHarvest_completion_date.isBefore(sowing_date)
						|| otherPlanSowing_date.isAfter(harvest_completion_date))) {
					continue outerLoop;
				}
			}
			// 上記の検証を通過したsectionのみをsearchSectionsに追加
			searchSections.add(section);
		}
		return searchSections;
	}

	// PlanエンティティからPlanFormエンティティの取得
	public PlanForm getPlanForm(Plan plan) throws IOException {
		modelMapper.getConfiguration().setAmbiguityIgnored(true);
		modelMapper.typeMap(Plan.class, PlanForm.class).addMappings(mapper -> mapper.skip(PlanForm::setCropName));
		modelMapper.typeMap(Plan.class, PlanForm.class).addMappings(mapper -> mapper.skip(PlanForm::setSectionName));
		PlanForm form = modelMapper.map(plan, PlanForm.class);
		String cropName = plan.getCrop().getName();
		String sectionName = plan.getSection().getName();
		form.setCropName(cropName);
		form.setSectionName(sectionName);
		return form;
	}

	// PlanListからGantList<PlanForm>（ガントチャート表示用）を取得
	private List<PlanForm> getGantList(Integer year, List<Plan> list) throws IOException {
		List<PlanForm> gantList = new ArrayList<>();
		for (Plan plan : list) {
			PlanForm planForm = getPlanForm(plan);
			gantList.add(planForm);
		}
		// yearの指定が場合、ガントチャートのX軸の表示幅を一年間に指定する為、ダミーのエンティティを一つ生成
		if (year != null) {
			// 年の最初の日付
			LocalDate startDate = LocalDate.ofYearDay(year, 1);
			// 年の最後の日付
			int dayOfYear = Year.of(year).isLeap() ? 366 : 365;
			LocalDate endDate = LocalDate.ofYearDay(year, dayOfYear);
			PlanForm start_end = new PlanForm((long) 1000, "start", "", startDate, endDate, true);
			gantList.add(start_end);
		}
		return gantList;
	}

	// PlanListからYearList<Integer>（検索formに表示する年のリスト）を取得
	private Set<Integer> getYearList(Long userId) {
		// 重複を許可しないHashSet型のリストを生成
		Set<Integer> yearList = new HashSet<>();
		List<Plan> list = repository.findAllByUserIdOrderByUpdatedAtAsc(userId);
		for (Plan plan : list) {
			yearList.add(plan.getSowing_date().getYear());
			yearList.add(plan.getHarvest_completion_date().getYear());
		}
		return yearList;
	}

	// planのその年に栽培期間が含まれるplanのリストを取得
	private List<Plan> getListOfYear(Integer year, List<Plan> list) {
		list.removeIf(plan -> !(plan.getSowing_date().getYear() == year
				|| plan.getHarvest_completion_date().getYear() == year));
		return list;
	}

}
