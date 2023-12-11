package com.example.portfolio.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.time.LocalDate;
import java.time.Month;
import java.time.MonthDay;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.Valid;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;

import com.example.portfolio.repository.CropRepository;
import com.example.portfolio.repository.PlanRepository;
import com.example.portfolio.repository.SectionRepository;
import com.example.portfolio.repository.CropImageReposiory;
import com.example.portfolio.entity.Crop;
import com.example.portfolio.entity.CropImage;
import com.example.portfolio.entity.Plan;
import com.example.portfolio.entity.Section;
import com.example.portfolio.entity.UserInf;
import com.example.portfolio.form.CropForm;
import com.example.portfolio.form.CropImageForm;
import com.example.portfolio.form.PlanForm;
import com.example.portfolio.form.UserForm;

import org.springframework.beans.BeanUtils;

@Controller
//@SessionAttributes("cropform") // 必要なければ削除
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

	// 作物データ新規登録formの表示
	@GetMapping(path = "/plans/new")
	public String newPlan(@RequestParam(required = false) Long cropId, Model model) {
		PlanForm form = new PlanForm();
		if (cropId != null) {
			Optional<Crop> optionalCrop = cropRepository.findById(cropId);
			Crop crop = optionalCrop.orElseThrow(() -> new RuntimeException("Crop not found")); //
			form.setCropName(crop.getName());
		}
		model.addAttribute("form", form);
		return "plans/new";
	}

	// formから送られた栽培計画データを登録
	@RequestMapping(value = "/plan", method = RequestMethod.POST)
	public String create(@RequestParam(name = "cmd") String cmd, Principal principal,
			@Validated @ModelAttribute("form") PlanForm form, BindingResult result, Model model,
			RedirectAttributes attributes) throws IOException {
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		Crop crop = cropRepository.findByNameAndUserId(form.getCropName(), user.getUserId());
		List<Section> searchSections = new ArrayList<>();
		if (form.getSowing_date() != null && form.getHarvest_completion_date() != null) {
			searchSections = availableSctionGenerater(user.getUserId(), form.getSowing_date(),
					form.getHarvest_completion_date(), form.getId());
		}
		if ("search".equals(cmd)) {
			List<Crop> searchCrops = new ArrayList<>();
			if (form.getCropName() == null || form.getCropName().isEmpty()) {
				searchCrops = cropRepository.findAllByUserIdOrderByUpdatedAtDesc(user.getUserId());
			} else if (form.getCropName() != null) {
				searchCrops = cropRepository.findByNameContainingAndUserId(form.getCropName(), user.getUserId());
			}
			if (searchCrops.isEmpty()) {
				model.addAttribute("searchCropsMessage", "その作物は見つかりませんでした。");
			}
			model.addAttribute("searchCrops", searchCrops);
			model.addAttribute("searchSections", searchSections);
			model.addAttribute("form", form);
			return "plans/new";
		}
		if ("calculate".equals(cmd)) {
			if (form.getCropName() == null || form.getCropName().isEmpty()) {
				FieldError fieldError = new FieldError(result.getObjectName(), "cropName", "作物名を入力してください。");
				result.addError(fieldError);
			} else if (crop == null) {
				FieldError fieldError = new FieldError(result.getObjectName(), "cropName", "作物名に誤りがあります。");
				result.addError(fieldError);
			}
			if (form.getSowing_date() == null) {
				FieldError fieldError = new FieldError(result.getObjectName(), "cropName", "播種日を入力してください。");
				result.addError(fieldError);
			}
			if (result.hasErrors()) {
				model.addAttribute("searchSections", searchSections);
				model.addAttribute("form", form);
				model.addAttribute("hasMessage", true);
				model.addAttribute("class", "alert-danger");
				model.addAttribute("message", "収穫完了予定日の算出に失敗しました。");
				return "plans/new";
			}

			LocalDate sowing_date = form.getSowing_date();
			int cultivationp_period = crop.getCultivationp_period();
			LocalDate harvest_completion_date = sowing_date.plusDays(cultivationp_period);
			form.setHarvest_completion_date(harvest_completion_date);
			searchSections = availableSctionGenerater(user.getUserId(), form.getSowing_date(), harvest_completion_date,
					form.getId());
			model.addAttribute("searchSections", searchSections);
			model.addAttribute("form", form);
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-info");
			model.addAttribute("message", "収穫完了予定日の算出に成功しました。");
			return "plans/new";
		}

		if ("select".equals(cmd)) {
			if (form.getSowing_date() == null) {
				FieldError fieldError = new FieldError(result.getObjectName(), "cropName", "播種日を入力してください。");
				result.addError(fieldError);
			}
			// 収穫完了予定日が入力されていない場合のエラーの追加
			if (form.getHarvest_completion_date() == null) {
				FieldError fieldError = new FieldError(result.getObjectName(), "harvest_completion_date",
						"収穫完了予定日を入力して下さい。");
				result.addError(fieldError);
			}

			if (result.hasErrors()) {
				model.addAttribute("searchSections", searchSections);
				model.addAttribute("form", form);
				model.addAttribute("hasMessage", true);
				model.addAttribute("class", "alert-danger");
				model.addAttribute("message", "利用可能な区画の取得に失敗しました。");
				return "plans/new";
			}

			searchSections = availableSctionGenerater(user.getUserId(), form.getSowing_date(),
					form.getHarvest_completion_date(), form.getId());
			if (searchSections.isEmpty()) {
				model.addAttribute("searchSectionsMessage", "利用可能な区画はありません。");
			}
			model.addAttribute("searchSections", searchSections);
			model.addAttribute("form", form);
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-info");

			model.addAttribute("message", "利用可能な区画の取得に成功しました。");
			return "plans/new";
		}

		if ("register".equals(cmd)) {
			Section section = sectionRepository.findByNameAndUserId(form.getSectionName(), user.getUserId());

			if (form.getCropName() == null || form.getCropName().isEmpty()) {
				FieldError fieldError = new FieldError(result.getObjectName(), "cropName", "作物名を入力してください。");
				result.addError(fieldError);
			} else if (crop == null) {
				FieldError fieldError = new FieldError(result.getObjectName(), "cropName", "作物名に誤りがあります。");
				result.addError(fieldError);
			}
			if (form.getSowing_date() == null) {
				FieldError fieldError = new FieldError(result.getObjectName(), "cropName", "播種日を入力してください。");
				result.addError(fieldError);
			}
			// 収穫完了予定日が入力されていない場合のエラーの追加
			if (form.getHarvest_completion_date() == null) {
				FieldError fieldError = new FieldError(result.getObjectName(), "harvest_completion_date",
						"収穫完了予定日を入力して下さい。");
				result.addError(fieldError);
			}
			// 区画名が入力されていない場合に返すエラーの追加。
			if (form.getSectionName() == null || form.getSectionName().isEmpty()) {
				FieldError fieldError = new FieldError(result.getObjectName(), "sectionName", "区画を選択して下さい。");
				result.addError(fieldError);
			}

			// 区画名が誤っている場合に返すエラーの追加
			if (!(form.getSectionName() == null || form.getSectionName().isEmpty()) && section == null) {
				FieldError fieldError = new FieldError(result.getObjectName(), "sectionName", "区画名に誤りがあります。");
				result.addError(fieldError);
			}
			searchSections = availableSctionGenerater(user.getUserId(), form.getSowing_date(),
					form.getHarvest_completion_date(), form.getId());

			// 区画が条件を満たしていない場合のエラーを追加
			if (!(form.getSectionName() == null || form.getSectionName().isEmpty())
					&& !searchSections.contains(section)) {
				FieldError fieldError = new FieldError(result.getObjectName(), "sectionName", "この区画は使用不可です。");
				result.addError(fieldError);
			}

			if (result.hasErrors()) {
				model.addAttribute("searchSections", searchSections);
				model.addAttribute("form", form);
				model.addAttribute("hasMessage", true);
				model.addAttribute("class", "alert-danger");
				model.addAttribute("message", "作物栽培計画の登録に失敗しました。");
				return "plans/new";
			}

			Plan entity = new Plan();
			entity.setUserId(user.getUserId());
			entity.setCropId(crop.getId());
			entity.setSowing_date(form.getSowing_date());
			entity.setHarvest_completion_date(form.getHarvest_completion_date());
			entity.setSectionId(section.getId());
			entity.setCompletion(form.isCompletion());
			// entityの保存
			repository.saveAndFlush(entity);
			attributes.addFlashAttribute("hasMessage", true);
			attributes.addFlashAttribute("class", "alert-info");
			attributes.addFlashAttribute("message", "栽培計画データ登録に成功しました。");
			return "redirect:/plans/detail/" + entity.getId();

		}
		model.addAttribute("searchSections", searchSections);
		model.addAttribute("form", form);
		model.addAttribute("hasMessage", true);
		model.addAttribute("class", "alert-danger");
		model.addAttribute("message", "作物栽培計画の登録に失敗しました。");
		return "plans/new";
	}

	// 栽培計画データ編集
	@RequestMapping(value = "/edit", method = RequestMethod.POST)
	public String edit(@RequestParam(name = "cmd") String cmd, Principal principal,
			@Validated @ModelAttribute("form") PlanForm form, BindingResult result, Model model,
			RedirectAttributes attributes) throws IOException {
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		Crop crop = cropRepository.findByNameAndUserId(form.getCropName(), user.getUserId());
		List<Section> searchSections = new ArrayList<>();
		if (form.getSowing_date() != null && form.getHarvest_completion_date() != null) {
			searchSections = availableSctionGenerater(user.getUserId(), form.getSowing_date(),
					form.getHarvest_completion_date(), form.getId());
		}
		if ("search".equals(cmd)) {
			List<Crop> searchCrops = new ArrayList<>();
			if (form.getCropName() == null) {
				searchCrops = cropRepository.findAllByUserIdOrderByUpdatedAtDesc(user.getUserId());
			} else if (form.getCropName() != null) {
				searchCrops = cropRepository.findByNameContainingAndUserId(form.getCropName(), user.getUserId());
			}
			if (searchCrops.isEmpty()) {
				model.addAttribute("searchCropsMessage", "その作物は見つかりませんでした。");
			}
			model.addAttribute("searchCrops", searchCrops);
			model.addAttribute("searchSections", searchSections);
			model.addAttribute("form", form);
			return "plans/edit";
		}
		if ("calculate".equals(cmd)) {

			if (form.getCropName() == null || form.getCropName().isEmpty()) {
				FieldError fieldError = new FieldError(result.getObjectName(), "cropName", "作物名を入力してください。");
				result.addError(fieldError);
			} else if (crop == null) {
				FieldError fieldError = new FieldError(result.getObjectName(), "cropName", "作物名に誤りがあります。");
				result.addError(fieldError);
			}
			if (form.getSowing_date() == null) {
				FieldError fieldError = new FieldError(result.getObjectName(), "cropName", "播種日を入力してください。");
				result.addError(fieldError);
			}

			if (result.hasErrors()) {
				model.addAttribute("searchSections", searchSections);
				model.addAttribute("form", form);
				model.addAttribute("hasMessage", true);
				model.addAttribute("class", "alert-danger");
				model.addAttribute("message", "収穫完了予定日の算出に失敗しました。");
				return "plans/edit";
			}

			LocalDate sowing_date = form.getSowing_date();
			int cultivationp_period = crop.getCultivationp_period();
			LocalDate harvest_completion_date = sowing_date.plusDays(cultivationp_period);
			form.setHarvest_completion_date(harvest_completion_date);
			searchSections = availableSctionGenerater(user.getUserId(), form.getSowing_date(), harvest_completion_date,
					form.getId());
			model.addAttribute("form", form);
			model.addAttribute("searchSections", searchSections);
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-info");
			model.addAttribute("message", "収穫完了予定日の算出に成功しました。");
			return "plans/edit";
		}

		if ("select".equals(cmd)) {
			if (form.getSowing_date() == null) {
				FieldError fieldError = new FieldError(result.getObjectName(), "cropName", "播種日を入力してください。");
				result.addError(fieldError);
			}
			// 収穫完了予定日が入力されていない場合のエラーの追加
			if (form.getHarvest_completion_date() == null) {
				FieldError fieldError = new FieldError(result.getObjectName(), "harvest_completion_date",
						"収穫完了予定日を入力して下さい。");
				result.addError(fieldError);
			}
			if (result.hasErrors()) {
				model.addAttribute("form", form);
				model.addAttribute("hasMessage", true);
				model.addAttribute("class", "alert-danger");
				model.addAttribute("message", "利用可能な区画の取得に失敗しました。");
				return "plans/edit";
			}
			searchSections = availableSctionGenerater(user.getUserId(), form.getSowing_date(),
					form.getHarvest_completion_date(), form.getId());
			if (searchSections.isEmpty()) {
				model.addAttribute("searchSectionsMessage", "利用可能な区画はありません。");
			}
			model.addAttribute("searchSections", searchSections);
			model.addAttribute("form", form);
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-info");
			model.addAttribute("message", "利用可能な区画の取得に成功しました。");
			return "plans/edit";
		}

		if ("register".equals(cmd)) {
			Section section = sectionRepository.findByNameAndUserId(form.getSectionName(), user.getUserId());

			if (form.getCropName() == null || form.getCropName().isEmpty()) {
				FieldError fieldError = new FieldError(result.getObjectName(), "cropName", "作物名を入力してください。");
				result.addError(fieldError);
			} else if (crop == null) {
				FieldError fieldError = new FieldError(result.getObjectName(), "cropName", "作物名に誤りがあります。");
				result.addError(fieldError);
			}
			if (form.getSowing_date() == null) {
				FieldError fieldError = new FieldError(result.getObjectName(), "cropName", "播種日を入力してください。");
				result.addError(fieldError);
			}

			// 収穫完了予定日が入力されていない場合のエラーの追加
			if (form.getHarvest_completion_date() == null) {
				FieldError fieldError = new FieldError(result.getObjectName(), "harvest_completion_date",
						"収穫完了予定日を入力して下さい。");
				result.addError(fieldError);
			}
			// 区画名が入力されていない場合に返すエラーの追加。
			if (form.getSectionName() == null || form.getSectionName().isEmpty()) {
				FieldError fieldError = new FieldError(result.getObjectName(), "sectionName", "区画を選択して下さい。");
				result.addError(fieldError);
			}

			// 区画名が誤っている場合に返すエラーの追加
			if (!(form.getSectionName() == null || form.getSectionName().isEmpty()) && section == null) {
				FieldError fieldError = new FieldError(result.getObjectName(), "sectionName", "区画名に誤りがあります。");
				result.addError(fieldError);
			}
			searchSections = availableSctionGenerater(user.getUserId(), form.getSowing_date(),
					form.getHarvest_completion_date(), form.getId());

			// 区画が条件を満たしていない場合のエラーを追加
			if (!(form.getSectionName() == null || form.getSectionName().isEmpty())
					&& !searchSections.contains(section)) {
				FieldError fieldError = new FieldError(result.getObjectName(), "sectionName", "この区画は使用不可です。");
				result.addError(fieldError);
			}

			if (result.hasErrors()) {
				model.addAttribute("searchSections", searchSections);
				model.addAttribute("form", form);
				model.addAttribute("hasMessage", true);
				model.addAttribute("class", "alert-danger");
				model.addAttribute("message", "作物栽培計画の編集に失敗しました。");
				return "plans/edit";
			}
			Optional<Plan> optionalPlan = repository.findById(form.getId());
			Plan entity = optionalPlan.orElseThrow(() -> new RuntimeException("Crop not found")); // もし Optional //
																									// //
			entity.setUserId(user.getUserId());
			entity.setCropId(crop.getId());
			entity.setSowing_date(form.getSowing_date());
			entity.setHarvest_completion_date(form.getHarvest_completion_date());
			entity.setSectionId(section.getId());
			entity.setCompletion(form.isCompletion());
			// entityの保存
			repository.saveAndFlush(entity);
			// 画像を表示するたmodelめのimageListを作成
			attributes.addFlashAttribute("hasMessage", true);
			attributes.addFlashAttribute("class", "alert-info");
			attributes.addFlashAttribute("message", "栽培計画データ編集に成功しました。");
			return "redirect:/plans/detail/" + entity.getId();
		}
		model.addAttribute("searchSections", searchSections);
		model.addAttribute("form", form);
		model.addAttribute("hasMessage", true);
		model.addAttribute("class", "alert-danger");
		model.addAttribute("message", "作物栽培計画の登録に失敗しました。");
		return "plans/edit";
	}

	// 区画が使用可能な区画のリスト生成
	private List<Section> availableSctionGenerater(Long userId, LocalDate sowing_date,
			LocalDate harvest_completion_date, Long planId) throws IOException {
		List<Section> sectionList = sectionRepository.findAllByUserIdOrderByUpdatedAtDesc(userId);
		List<Section> searchSections = new ArrayList<>();
		outerLoop: for (Section usersSection : sectionList) {
			List<Plan> planList = usersSection.getPlans();
			if (planId != null) {
				Optional<Plan> optionalPlan = repository.findById(planId);
				Plan thisPlan = optionalPlan.orElseThrow(() -> new RuntimeException("Crop not found"));
				if (planList.contains(thisPlan)) {
					planList.remove(thisPlan);
				}
			}
			for (Plan plan : planList) {
				LocalDate otherPlanSowing_date = plan.getSowing_date();
				LocalDate otherPlanHarvest_completion_date = plan.getHarvest_completion_date();
				if (!(otherPlanHarvest_completion_date.isBefore(sowing_date)
						|| otherPlanSowing_date.isAfter(harvest_completion_date))) {
					continue outerLoop;
				}
			}
			searchSections.add(usersSection);
		}
		return searchSections;
	}

//	// 完了済みの計画か否かの判定
//	private boolean completionJudgement(LocalDate harvest_completion_date) throws IOException {
//		if (harvest_completion_date.isBefore(LocalDate.now())) {
//			return true;
//		}
//		return false;
//	}

	@GetMapping(path = "/plans/detail/{planId}")
	public String showDetail(@PathVariable Long planId, Model model) throws IOException {
		Optional<Plan> optionalPlan = repository.findById(planId);
		Plan plan = optionalPlan.orElseThrow(() -> new RuntimeException("Crop not found")); // もし Optional // //
																							// が空の場合は例外をスローするなどの対
		model.addAttribute("plan", plan);
		return "plans/detail";
	}

	// 編集画面
	@GetMapping(path = "/plans/edit/{planId}")
	public String showEditPage(@PathVariable Long planId, Model model, Principal principal) throws IOException {
		Optional<Plan> optionalPlan = repository.findById(planId);
		Plan plan = optionalPlan.orElseThrow(() -> new RuntimeException("Crop not found")); // もし Optional // //
																							// が空の場合は例外をスローするなどの対処
		PlanForm form = getPlan(plan);
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		List<Section> searchSections = availableSctionGenerater(user.getUserId(), form.getSowing_date(),
				form.getHarvest_completion_date(), form.getId());

		model.addAttribute("searchSections", searchSections);
		model.addAttribute("form", form);
		return "plans/edit";
	}

	public PlanForm getPlan(Plan plan) throws IOException {
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

	@GetMapping(path = "/plans/delete/{planId}")
	public String delete(@PathVariable Long planId, Model model, RedirectAttributes redirAttrs) throws IOException {
		repository.deleteById(planId);
		redirAttrs.addFlashAttribute("hasMessage", true);
		redirAttrs.addFlashAttribute("class", "alert-info");
		redirAttrs.addFlashAttribute("message", "作物データの削除に成功しました。");
		return "redirect:/plans/list";
	}

	@GetMapping(path = "/plans/list")
	public String showList(Principal principal, Model model) throws IOException {
		// User情報の取得
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		List<Plan> list = repository.findAllByUserIdOrderByUpdatedAtDesc(user.getUserId());
		List<Section> sectionList = sectionRepository.findAllByUserIdOrderByUpdatedAtDesc(user.getUserId());
		List<PlanForm> gantList = getGantList(LocalDate.now().getYear(), list);
		Set<Integer> yearList = getYearList(list);
		model.addAttribute("yearList", yearList);
		model.addAttribute("gantList", gantList);
		model.addAttribute("list", list);
		model.addAttribute("sectionList", sectionList);
		return "/plans/list";
	}

	// 作物検索機能
	@RequestMapping(value = "/plans/search", method = RequestMethod.POST)
	public String searchCrops(Principal principal, @RequestParam(name = "option", required = false) String option,
			@RequestParam(name = "keyword", required = false) String keyword,
			@RequestParam(name = "sectionName", required = false) String sectionName,
			@RequestParam(name = "year", required = false) Integer year, Model model, RedirectAttributes redirAttrs)
			throws IOException {

		// User情報の取得
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		List<Section> sectionList = sectionRepository.findAllByUserIdOrderByUpdatedAtDesc(user.getUserId());
		
		// 該当のユーザーのすべての作物のリストを作成
		List<Plan> list = new ArrayList<>();
		if (sectionName == null || sectionName.isEmpty()) {
			if (keyword == null) {
				if ("progress".equals(option)) {
					list = repository.findAllByUserIdAndCompletionFalseOrderByUpdatedAtDesc(user.getUserId());

				} else if ("completion".equals(option)) {
					list = repository.findAllByUserIdAndCompletionTrueOrderByUpdatedAtDesc(user.getUserId());
				} else {
					list = repository.findAllByUserIdOrderByUpdatedAtDesc(user.getUserId());
				}
			}
			if (keyword != null) {
				if ("progress".equals(option)) {
					list = repository.findAllByUserIdAndCompletionFalseAndCropNameContainingOrderByUpdatedAtDesc(
							user.getUserId(), keyword);
				} else if ("completion".equals(option)) {
					list = repository.findAllByUserIdAndCompletionTrueAndCropNameContainingOrderByUpdatedAtDesc(
							user.getUserId(), keyword);
				} else {
					list = repository.findAllByUserIdAndCropNameContainingOrderByUpdatedAtDesc(user.getUserId(),
							keyword);
				}
			}
		} else if (sectionName != null) {
			if (keyword == null) {
				if ("progress".equals(option)) {
					list = repository.findAllByUserIdAndCompletionFalseAndSectionNameOrderByUpdatedAtDesc(
							user.getUserId(), sectionName);
				} else if ("completion".equals(option)) {
					list = repository.findAllByUserIdAndCompletionTrueAndSectionNameOrderByUpdatedAtDesc(
							user.getUserId(), sectionName);

				} else {
					list = repository.findAllByUserIdAndSectionNameOrderByUpdatedAtDesc(user.getUserId(), sectionName);
				}
			}
			if (keyword != null) {
				if ("progress".equals(option)) {
					list = repository
							.findAllByUserIdAndCompletionFalseAndCropNameContainingAndSectionNameOrderByUpdatedAtDesc(
									user.getUserId(), keyword, sectionName);
				} else if ("completion".equals(option)) {
					list = repository
							.findAllByUserIdAndCompletionTrueAndCropNameContainingAndSectionNameOrderByUpdatedAtDesc(
									user.getUserId(), keyword, sectionName);
				} else {
					list = repository.findAllByUserIdAndCropNameContainingAndSectionNameOrderByUpdatedAtDesc(
							user.getUserId(), keyword, sectionName);
				}
			}
		}
		if(year != null) {
			list = getListOfYear(year, list);
		}
		if (list.isEmpty()) {
			model.addAttribute("message", "その栽培計画は見つかりませんでした。");
		}
		List<Plan> allList = repository.findAllByUserIdOrderByUpdatedAtDesc(user.getUserId());
		Set<Integer>yearList = getYearList(allList);
		List<PlanForm> gantList = getGantList(year, list);
		model.addAttribute("gantList", gantList);
		model.addAttribute("list", list);
		model.addAttribute("yearList", yearList);
		model.addAttribute("year", year);
		model.addAttribute("sectionList", sectionList);
		model.addAttribute("sectionName", sectionName);
		model.addAttribute("option", option);
		model.addAttribute("keyword", keyword);
		return "/plans/list";
	}

	// PlanListからGantList<PlanForm>を得る
	private List<PlanForm> getGantList(Integer year, List<Plan> list) throws IOException {
		List<PlanForm> gantList = new ArrayList<>();
		for (Plan plan : list) {
			PlanForm planForm = getPlan(plan);
			gantList.add(planForm);
		}
		LocalDate startDate = null;
		LocalDate endDate = null;

		if(year == null) {
			startDate = LocalDate.ofYearDay(LocalDate.now().getYear(), 1);
			int dayOfYear = Year.of(LocalDate.now().getYear()).isLeap() ? 366 : 365;
			endDate = LocalDate.ofYearDay(LocalDate.now().getYear(), dayOfYear);
		}
		if(year != null) {
		// 年の最初の日付
		startDate = LocalDate.ofYearDay(year, 1);
		// 年の最後の日付
		int dayOfYear = Year.of(year).isLeap() ? 366 : 365;
		endDate = LocalDate.ofYearDay(year, dayOfYear);
		}
		PlanForm startDatePlan = new PlanForm((long) 1000, "start", "", startDate, startDate, true);
		PlanForm endDatePlan = new PlanForm((long) 0, "end", "", endDate, endDate, true);
		gantList.add(startDatePlan);
		gantList.add(endDatePlan);
		return gantList;
	}

	// PlanListからYearList<Integer>を得る
	private Set<Integer> getYearList(List<Plan> list) {
		// 重複を許さないリストを作成
		Set<Integer> yearList = new HashSet<>();
		for (Plan plan : list) {
			yearList.add(plan.getSowing_date().getYear());
			yearList.add(plan.getHarvest_completion_date().getYear());
		}
		return yearList;
	}

	private List<Plan> getListOfYear(Integer year, List<Plan> list) {
	    list.removeIf(plan -> !(plan.getSowing_date().getYear() == year || plan.getHarvest_completion_date().getYear() == year));
	    return list;
	}

	@GetMapping("/confirmation")
	public String showGanttChart(Principal principal, Model model) {
		// User情報の取得
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		List<Section> sectionList = sectionRepository.findAllByUserIdOrderByUpdatedAtDesc(user.getUserId());
		model.addAttribute("sectionList", sectionList);

		return "plans/confirmation";

	}

}
