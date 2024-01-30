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

/**
 * 栽培計画に関する操作（登録、編集、表示、削除）を担当するコントローラークラス
 */
@Controller
public class PlanController {

	// ロガーの初期化
	protected static Logger log = LoggerFactory.getLogger(PlanController.class);

	// ModelMapperの注入
	@Autowired
	private ModelMapper modelMapper;

	// 作物リポジトリの注入
	@Autowired
	CropRepository cropRepository;

	// 区画リポジトリの注入
	@Autowired
	SectionRepository sectionRepository;

	// 栽培計画リポジトリの注入
	@Autowired
	PlanRepository repository;

	// 栽培日誌リポジトリの注入
	@Autowired
	DiaryRepository diaryRepository;

	/**
	 * 栽培計画登録画面の表示
	 *
	 * @param cropId 作物ID（指定がなくてもいい）
	 * @param model  ビューで使用するモデル
	 * @return 栽培計画登録画面
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	@GetMapping(path = "/plans/new")
	public String newPlan(@RequestParam(required = false) Long cropId, Model model) throws IOException {
		// 栽培計画フォームを初期化
		PlanForm form = new PlanForm();
		// 作物IDの指定がある場合の処理
		if (cropId != null) {
			// 作物IDから作物を取得
			Optional<Crop> optionalCrop = cropRepository.findById(cropId);
			Crop crop = optionalCrop.orElseThrow(() -> new RuntimeException("Crop not found"));
			// 取得した作物の作物名を、初期化した栽培計画フォームに設定
			form.setCropName(crop.getName());
		}
		// 栽培計画フォームをモデルに設定
		model.addAttribute("form", form);
		// 栽培計画登録画面を返す
		return "plans/new";
	}

	/**
	 * 栽培計画の登録<br>
	 * 栽培計画登録画面から送信されたフォームデータの検証、保存を行う。<br>
	 * また、作物の検索、収穫完了予定日の取得、使用可能な区画の取得も行う。
	 *
	 * @param principal  認証情報
	 * @param cmd        コマンド（値は、"search"、"calculate"、"select"、"register"の4つ）
	 * @param form       栽培計画登録フォームデータ
	 * @param result     フォーム検証の結果
	 * @param model      ビューで使用するモデル
	 * @param redirAttrs リダイレクト属性
	 * @return 検証結果に基づく画面
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	@PostMapping(path = "/plan")
	public String create(Principal principal, @RequestParam(name = "cmd") String cmd,
			@Validated @ModelAttribute("form") PlanForm form, BindingResult result, Model model,
			RedirectAttributes redirAttrs) throws IOException {
		// 認証情報からユーザーを取得
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		// フォームデータを変数に代入
		Long userId = user.getUserId();
		String cropName = form.getCropName();
		String sectionName = form.getSectionName();
		LocalDate sowing_date = form.getSowing_date();
		LocalDate harvest_completion_date = form.getHarvest_completion_date();
		boolean completion = form.isCompletion();
		// フォームデータの区画名とユーザーIDから区画を取得
		Section section = sectionRepository.findByNameAndUserId(sectionName, userId);
		// フォームデータの作物名とユーザーIDから作物を取得
		Crop crop = cropRepository.findByNameAndUserId(cropName, userId);
		// 使用可能な区画リストを初期化
		List<Section> searchSections = new ArrayList<>();
		// フォームデータの播種日と収穫完了予定日に値が入力されていれば、使用可能な区画を取得し、使用可能な区画リストに代入
		if (sowing_date != null && harvest_completion_date != null) {
			searchSections = availableSctionGenerater(user.getUserId(), sowing_date, harvest_completion_date, null);
		}
		// 作物の検索
		// cmdの値に"search"が指定された場合の処理
		if ("search".equals(cmd)) {
			// 検索結果の作物を表示する為の、作物リストを初期化
			List<Crop> searchCrops = new ArrayList<>();
			// 作物名の値が指定されていない場合、ユーザーに対応する全ての作物を作物リストに代入
			if (cropName == null || cropName.isEmpty()) {
				searchCrops = cropRepository.findAllByUserIdOrderByUpdatedAtAsc(userId);
				// 作物名の値が指定されている場合、指定された値のキーワードを作物名に含みユーザーに対応する作物を作物リストに代入
			} else {
				searchCrops = cropRepository.findAllByNameContainingAndUserId(cropName, userId);
			}
			// 作物リストが空の場合、モデルにメッセージを設定
			if (searchCrops.isEmpty()) {
				model.addAttribute("searchCropsMessage", "その作物は見つかりませんでした。");
			}
			// 作物リストをモデルに設定
			model.addAttribute("searchCrops", searchCrops);
			// 使用可能な区画リストをモデルに設定
			model.addAttribute("searchSections", searchSections);
			// 栽培計画登録画面を返す
			return "plans/new";
		}
		// 作物名と播種日から収穫完了予定日を取得
		// cmdの値に"calculate"が指定された場合の処理
		if ("calculate".equals(cmd)) {
			// 作物名が入力されていない場合に返すエラーの追加
			if (cropName == null || cropName.isEmpty()) {
				FieldError fieldError = new FieldError(result.getObjectName(), "cropName", "作物名を入力してください。");
				result.addError(fieldError);
				// 上記以外で、入力された作物名が登録されてない場合に返すエラーの追加
			} else if (crop == null) {
				FieldError fieldError = new FieldError(result.getObjectName(), "cropName", "作物名に誤りがあります。");
				result.addError(fieldError);
			}
			// 播種日が入力されていない場合に返すエラーの追加
			if (sowing_date == null) {
				FieldError fieldError = new FieldError(result.getObjectName(), "sowing_date", "播種日を入力してください。");
				result.addError(fieldError);
			}
			// エラーがあった場合、栽培計画登録画面を返しエラーを表示
			if (result.hasErrors()) {
				// 使用可能な区画リストをモデルに設定
				model.addAttribute("searchSections", searchSections);
				// アラートメッセージをモデルに設定
				model.addAttribute("hasMessage", true);
				model.addAttribute("class", "alert-danger");
				model.addAttribute("message", "収穫完了予定日の算出に失敗しました。");
				// 栽培計画登録画面を返す
				return "plans/new";
			}
			// エラーが無かった場合の処理
			// 作物の栽培日数を取得
			int cultivationp_period = crop.getCultivationp_period();
			// フォームデータの播種日と取得した作物の栽培日数から、収穫完了予定日を生成
			LocalDate newHarvest_completion_date = sowing_date.plusDays(cultivationp_period);
			// 生成した収穫完了予定日を栽培計画フォームに設定
			form.setHarvest_completion_date(newHarvest_completion_date);
			// 生成した収穫完了予定日を使用し、利用可能な区画を生成
			searchSections = availableSctionGenerater(userId, sowing_date, newHarvest_completion_date, null);
			// 使用可能な区画をモデルに設定
			model.addAttribute("searchSections", searchSections);
			// アラートメッセージをモデルに設定
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-info");
			model.addAttribute("message", "収穫完了予定日の算出に成功しました。");
			// 栽培計画登録画面を返す
			return "plans/new";
		}
		// 使用可能な区画の取得
		// cmdの値に"select"が指定された場合の処理
		if ("select".equals(cmd)) {
			// 播種日が入力されていない場合に返すエラーの追加
			if (sowing_date == null) {
				FieldError fieldError = new FieldError(result.getObjectName(), "sowing_date", "播種日を入力してください。");
				result.addError(fieldError);
				// 上記以外で、収穫完了予定日が入力されていない場合に返すエラーの追加
			} else if (harvest_completion_date == null) {
				FieldError fieldError = new FieldError(result.getObjectName(), "harvest_completion_date",
						"収穫完了予定日を入力してください。");
				result.addError(fieldError);
				// 上記以外で、収穫完了日に播種日より前の日付が入力されている場合に返すエラーの追加
			} else if (harvest_completion_date.isBefore(sowing_date)) {
				FieldError fieldError = new FieldError(result.getObjectName(), "harvest_completion_date",
						"播種日より後の日付を入力してください。");
				result.addError(fieldError);
			}
			// エラーがあった場合の処理
			if (result.hasErrors()) {
				// 使用可能な区画をモデルに設定
				model.addAttribute("searchSections", searchSections);
				// アラートメッセージをモデルに設定
				model.addAttribute("hasMessage", true);
				model.addAttribute("class", "alert-danger");
				model.addAttribute("message", "使用可能な区画の取得に失敗しました。");
				// 栽培計画登録画面を返す
				return "plans/new";
			}
			// エラーが無かった場合の処理
			// 使用可能な区画リストが空の場合、メッセージをモデルに設定
			if (searchSections.isEmpty()) {
				model.addAttribute("searchSectionsMessage", "使用可能な区画はありません。");
			}
			// 使用可能な区画リストをモデルに設定
			model.addAttribute("searchSections", searchSections);
			// アラートメッセージをモデルに設定
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-info");
			model.addAttribute("message", "使用可能な区画の取得に成功しました。");
			// 栽培計画登録画面を返す
			return "plans/new";
		}
		// 栽培計画の登録
		// cmdの値に"register"が指定された場合の処理
		if ("register".equals(cmd)) {
			// 作物名が入力されていない場合に返すエラーの追加
			if (cropName == null || cropName.isEmpty()) {
				FieldError fieldError = new FieldError(result.getObjectName(), "cropName", "作物名を入力してください。");
				result.addError(fieldError);
				// 上記以外で、入力された作物名が登録されてない場合に返すエラーの追加
			} else if (crop == null) {
				FieldError fieldError = new FieldError(result.getObjectName(), "cropName", "作物名に誤りがあります。");
				result.addError(fieldError);
			}
			// 播種日が入力されていない場合に返すエラーの追加
			if (sowing_date == null) {
				FieldError fieldError = new FieldError(result.getObjectName(), "sowing_date", "播種日を入力してください。");
				result.addError(fieldError);
				// 上記以外で、収穫完了予定日が入力されていない場合に返すエラーの追加
			} else if (harvest_completion_date == null) {
				FieldError fieldError = new FieldError(result.getObjectName(), "harvest_completion_date",
						"収穫完了予定日を入力して下さい。");
				result.addError(fieldError);
				// 上記以外で、収穫完了日に播種日より前の日付が入力されている場合に返すエラーの追加
			} else if (harvest_completion_date.isBefore(sowing_date)) {
				FieldError fieldError = new FieldError(result.getObjectName(), "harvest_completion_date",
						"播種日より後の日付を入力したください。");
				result.addError(fieldError);
				// 上記以外で、区画が選択されていない場合に返すエラーの追加
			} else if (sectionName == null || sectionName.isEmpty()) {
				FieldError fieldError = new FieldError(result.getObjectName(), "sectionName", "区画を選択して下さい。");
				result.addError(fieldError);
				// 上記以外で、選択された区画が登録されてない場合に返すエラーの追加
			} else if (section == null) {
				FieldError fieldError = new FieldError(result.getObjectName(), "sectionName", "区画名に誤りがあります。");
				result.addError(fieldError);
				// 上記以外で、選択された区画が使用不可の場合に返すエラーの追加
			} else if (!searchSections.contains(section)) {
				FieldError fieldError = new FieldError(result.getObjectName(), "sectionName", "この区画は使用不可です。");
				result.addError(fieldError);
			}
			// エラーがあった場合の処理
			if (result.hasErrors()) {
				// 使用可能な区画リストをモデルに設定
				model.addAttribute("searchSections", searchSections);
				// アラートメッセージをモデルに設定
				model.addAttribute("hasMessage", true);
				model.addAttribute("class", "alert-danger");
				model.addAttribute("message", "栽培計画の登録に失敗しました。");
				// 栽培計画登録画面を返す
				return "plans/new";
			}
			// エラーが無かった場合の処理
			// フォームデータで、栽培計画エンティティを生成
			Plan entity = new Plan(userId, crop.getId(), section.getId(), sowing_date, harvest_completion_date,
					completion);
			// 栽培計画エンティティを保存
			repository.saveAndFlush(entity);
			// アラートメッセージをリダイレクト先モデルに設定
			redirAttrs.addFlashAttribute("hasMessage", true);
			redirAttrs.addFlashAttribute("class", "alert-info");
			redirAttrs.addFlashAttribute("message", "栽培計画の登録が完了しました。");
			// リダイレクト先に登録した栽培計画IDのパラメータを渡し、栽培計画詳細画面を表示
			return "redirect:/plans/detail/" + entity.getId();
		}
		// 念のため、cmdの値が指定されなかった場合の処理
		// 使用可能な区画リストを、モデルに設定
		model.addAttribute("searchSections", searchSections);
		// アラートメッセージを、モデルに設定
		model.addAttribute("hasMessage", true);
		model.addAttribute("class", "alert-danger");
		model.addAttribute("message", "栽培計画の登録に失敗しました。");
		// 栽培計画登録画面を返す
		return "plans/new";
	}

	/**
	 * 栽培計画詳細画面を表示<br>
	 * 栽培計画に紐づく栽培日誌も同時に表示する
	 *
	 * @param planId 表示する栽培計画のID
	 * @param model  ビューで使用するモデル
	 * @return 栽培計画詳細画面
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	@GetMapping(path = "/plans/detail/{planId}")
	public String showDetail(@PathVariable Long planId, Model model) throws IOException {
		// 栽培計画IDから栽培計画を取得
		Optional<Plan> optionalPlan = repository.findById(planId);
		Plan entity = optionalPlan.orElseThrow(() -> new RuntimeException("Plan not found"));
		// 栽培計画IDにに紐づく栽培日誌を取得し、リストを生成
		List<Diary> list = diaryRepository.findAllByPlanIdOrderByUpdatedAtAsc(planId);
		// リストをモデルに設定
		model.addAttribute("list", list);
		// 栽培計画の情報をモデルに設定
		model.addAttribute("plan", entity);
		// 栽培計画詳細画面を返す
		return "plans/detail";
	}

	/**
	 * 栽培計画編集画面を表示
	 *
	 * @param planId    編集する栽培計画のID
	 * @param model     ビューで使用するモデル
	 * @param principal 認証情報
	 * @return 栽培計画編集画面
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	@GetMapping(path = "/plans/edit/{planId}")
	public String showEditPage(@PathVariable Long planId, Model model, Principal principal) throws IOException {
		// 栽培計画IDから栽培計画を取得
		Optional<Plan> optionalPlan = repository.findById(planId);
		Plan entity = optionalPlan.orElseThrow(() -> new RuntimeException("Plan not found"));
		// 栽培計画から栽培計画フォームを生成
		PlanForm form = getPlanForm(entity);
		// 認証情報からユーザーを取得
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		// 使用可能な区画リストを生成
		List<Section> searchSections = availableSctionGenerater(user.getUserId(), form.getSowing_date(),
				form.getHarvest_completion_date(), form.getId());
		// 使用可能な区画リストをモデルに設定
		model.addAttribute("searchSections", searchSections);
		// 栽培計画フォームをモデルに設定
		model.addAttribute("form", form);
		// 栽培計画編集画面を返す
		return "plans/edit";
	}

	/**
	 * 栽培計画の編集<br>
	 * 栽培計画編集画面から送信されたフォームデータの検証、更新を行う。<br>
	 * また、作物の検索、収穫完了予定日の取得、使用可能な区画の取得も行う。
	 *
	 * @param principal  認証情報
	 * @param cmd        コマンド（値は、"search"、"calculate"、"select"、"register"の4つ）
	 * @param form       栽培計画編集フォームデータ
	 * @param result     フォーム検証の結果
	 * @param model      ビューで使用するモデル
	 * @param redirAttrs リダイレクト属性
	 * @return 検証結果に基づく画面
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	@PostMapping(path = "/plans/edit-complete")
	public String edit(Principal principal, @RequestParam(name = "cmd") String cmd,
			@Validated @ModelAttribute("form") PlanForm form, BindingResult result, Model model,
			RedirectAttributes redirAttrs) throws IOException {
		// 認証情報からユーザーを取得
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		// フォームデータを変数に代入
		Long userId = user.getUserId();
		Long id = form.getId();
		String cropName = form.getCropName();
		String sectionName = form.getSectionName();
		LocalDate sowing_date = form.getSowing_date();
		LocalDate harvest_completion_date = form.getHarvest_completion_date();
		boolean completion = form.isCompletion();
		// フォームデータの作物名とユーザーIDから作物を取得
		Crop crop = cropRepository.findByNameAndUserId(cropName, userId);
		// フォームデータの区画名とユーザーIDから区画を取得
		Section section = sectionRepository.findByNameAndUserId(sectionName, userId);
		// 使用可能な区画リストを初期化
		List<Section> searchSections = new ArrayList<>();
		// フォームデータの播種日と収穫完了予定日に値が入力されていれば、使用可能な区画を取得し、使用可能な区画リストに代入
		if (sowing_date != null && harvest_completion_date != null) {
			searchSections = availableSctionGenerater(userId, sowing_date, harvest_completion_date, id);
		}
		// 作物の検索
		// cmdの値に"search"が指定された場合の処理
		if ("search".equals(cmd)) {
			// 検索結果の作物を表示する為の、作物リストを初期化
			List<Crop> searchCrops = new ArrayList<>();
			// 作物名の値が指定されていない場合、ユーザーに対応する全ての作物を作物リストに代入
			if (cropName == null || cropName.isEmpty()) {
				searchCrops = cropRepository.findAllByUserIdOrderByUpdatedAtAsc(userId);
			} else {
				// 作物名の値が指定されている場合、指定された値のキーワードを作物名に含みユーザーに対応する作物を作物リストに代入
				searchCrops = cropRepository.findAllByNameContainingAndUserId(cropName, userId);
			}
			// 作物リストが空の場合、モデルにメッセージを設定
			if (searchCrops.isEmpty()) {
				model.addAttribute("searchCropsMessage", "その作物は見つかりませんでした。");
			}
			// 作物リストをモデルに設定
			model.addAttribute("searchCrops", searchCrops);
			// 使用可能な区画リストをモデルに設定
			model.addAttribute("searchSections", searchSections);
			// 栽培計画編集画面を返す
			return "plans/edit";
		}
		// 作物名と播種日から収穫完了予定日を取得
		// cmdの値に"calculate"が指定された場合の処理
		if ("calculate".equals(cmd)) {
			// 作物名が入力されていない場合に返すエラーの追加
			if (cropName == null || cropName.isEmpty()) {
				FieldError fieldError = new FieldError(result.getObjectName(), "cropName", "作物名を入力してください。");
				result.addError(fieldError);
				// 上記以外で、入力された作物名が登録されてない場合に返すエラーの追加
			} else if (crop == null) {
				FieldError fieldError = new FieldError(result.getObjectName(), "cropName", "作物名に誤りがあります。");
				result.addError(fieldError);
			}
			// 播種日が入力されていない場合に返すエラーの追加
			if (sowing_date == null) {
				FieldError fieldError = new FieldError(result.getObjectName(), "sowing_date", "播種日を入力してください。");
				result.addError(fieldError);
			}
			// エラーがあった場合、栽培計画編集画面を返し、エラーを表示
			if (result.hasErrors()) {
				// 使用可能な区画リストをモデルに設定
				model.addAttribute("searchSections", searchSections);
				// アラートメッセージをモデルに設定
				model.addAttribute("hasMessage", true);
				model.addAttribute("class", "alert-danger");
				model.addAttribute("message", "収穫完了予定日の算出に失敗しました。");
				// 栽培計画編集画面を返す
				return "plans/edit";
			}
			// エラーが無かった場合の処理
			// 作物の栽培日数を取得
			int cultivationp_period = crop.getCultivationp_period();
			// フォームデータの播種日と取得した作物の栽培日数から、収穫完了予定日を生成
			LocalDate newHarvest_completion_date = sowing_date.plusDays(cultivationp_period);
			// 生成した収穫完了予定日を栽培計画フォームに設定
			form.setHarvest_completion_date(newHarvest_completion_date);
			// 生成した収穫完了予定日を使用し、使用可能な区画リストを取得
			searchSections = availableSctionGenerater(userId, sowing_date, newHarvest_completion_date, id);
			// 使用可能な区画リストをモデルに設定
			model.addAttribute("searchSections", searchSections);
			// アラートメッセージをモデルに設定
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-info");
			model.addAttribute("message", "収穫完了予定日の算出に成功しました。");
			// 栽培計画編集画面を返す
			return "plans/edit";
		}
		// 使用可能な区画の取得
		// cmdの値に"select"が指定された場合の処理
		if ("select".equals(cmd)) {
			// 播種日が入力されていない場合に返すエラーの追加
			if (sowing_date == null) {
				FieldError fieldError = new FieldError(result.getObjectName(), "sowing_date", "播種日を入力してください。");
				result.addError(fieldError);
				// 上記以外で、収穫完了予定日が入力されていない場合に返すエラーの追加
			} else if (harvest_completion_date == null) {
				FieldError fieldError = new FieldError(result.getObjectName(), "harvest_completion_date",
						"収穫完了予定日を入力してください。");
				result.addError(fieldError);
				// 上記以外で、収穫完了日に播種日より前の日付が入力されている場合に返すエラーの追加
			} else if (harvest_completion_date.isBefore(sowing_date)) {
				FieldError fieldError = new FieldError(result.getObjectName(), "harvest_completion_date",
						"播種日より後の日付を入力してください。");
				result.addError(fieldError);
			}
			// エラーがあった場合の処理
			if (result.hasErrors()) {
				// 使用可能な区画リストをモデルに設定
				model.addAttribute("searchSections", searchSections);
				// アラートメッセージをモデルに設定
				model.addAttribute("hasMessage", true);
				model.addAttribute("class", "alert-danger");
				model.addAttribute("message", "使用可能な区画の取得に失敗しました。");
				// 栽培計画編集画面を返す
				return "plans/edit";
			}
			// エラーが無かった場合の処理
			// 生成した使用可能な区画リストが空の場合、メッセージをモデルに設定
			if (searchSections.isEmpty()) {
				model.addAttribute("searchSectionsMessage", "使用可能な区画はありません。");
			}
			// 使用可能な区画リストをモデルに設定
			model.addAttribute("searchSections", searchSections);
			// アラートメッセージをモデルに設定
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-info");
			model.addAttribute("message", "使用可能な区画の取得に成功しました。");
			// 栽培計画編集画面を返す
			return "plans/edit";
		}

		// 栽培計画の編集
		// cmdの値に"register"が指定された場合の処理
		if ("register".equals(cmd)) {
			// 作物名が入力されていない場合に返すエラーの追加
			if (cropName == null || cropName.isEmpty()) {
				FieldError fieldError = new FieldError(result.getObjectName(), "cropName", "作物名を入力してください。");
				result.addError(fieldError);
				// 上記以外で、入力された作物名が登録されてない場合に返すエラーの追加
			} else if (crop == null) {
				FieldError fieldError = new FieldError(result.getObjectName(), "cropName", "作物名に誤りがあります。");
				result.addError(fieldError);
			}
			// 播種日が入力されていない場合に返すエラーの追加
			if (sowing_date == null) {
				FieldError fieldError = new FieldError(result.getObjectName(), "sowing_date", "播種日を入力してください。");
				result.addError(fieldError);
				// 上記以外で、収穫完了予定日が入力されていない場合に返すエラーの追加
			} else if (harvest_completion_date == null) {
				FieldError fieldError = new FieldError(result.getObjectName(), "harvest_completion_date",
						"収穫完了予定日を入力して下さい。");
				result.addError(fieldError);
				// 上記以外で、収穫完了日に播種日より前の日付が入力されている場合に返すエラーの追加
			} else if (harvest_completion_date.isBefore(sowing_date)) {
				FieldError fieldError = new FieldError(result.getObjectName(), "harvest_completion_date",
						"播種日より後の日付を入力したください。");
				result.addError(fieldError);
				// 上記以外で、区画が選択されていない場合に返すエラーの追加
			} else if (sectionName == null || sectionName.isEmpty()) {
				FieldError fieldError = new FieldError(result.getObjectName(), "sectionName", "区画を選択して下さい。");
				result.addError(fieldError);
				// 上記以外で、選択された区画が登録されてない場合に返すエラーの追加
			} else if (section == null) {
				FieldError fieldError = new FieldError(result.getObjectName(), "sectionName", "区画名に誤りがあります。");
				result.addError(fieldError);
				// 上記以外で、選択区画が使用不可の場合に返すエラーの追加
			} else if (!searchSections.contains(section)) {
				FieldError fieldError = new FieldError(result.getObjectName(), "sectionName", "この区画は使用不可です。");
				result.addError(fieldError);
			}
			// エラーがあった場合、栽培計画編集画面を返し、エラーを表示
			if (result.hasErrors()) {
				// 使用可能な区画をモデルに設定
				model.addAttribute("searchSections", searchSections);
				// アラートメッセージをモデルに設定
				model.addAttribute("hasMessage", true);
				model.addAttribute("class", "alert-danger");
				model.addAttribute("message", "栽培計画の編集に失敗しました。");
				// 栽培計画編集画面を返す
				return "plans/edit";
			}
			// エラーが無かった場合の処理
			// フォームデータのIDから栽培計画を取得
			Optional<Plan> optionalPlan = repository.findById(id);
			Plan entity = optionalPlan.orElseThrow(() -> new RuntimeException("Plan not found"));
			// フォームデータで栽培計画エンティティを更新
			entity.setCropId(crop.getId());
			entity.setSowing_date(sowing_date);
			entity.setHarvest_completion_date(harvest_completion_date);
			entity.setSectionId(section.getId());
			entity.setCompletion(completion);
			// 更新された栽培計画エンティティを保存
			repository.saveAndFlush(entity);
			// アラートメッセージをリダイレクト先のモデルに設定
			redirAttrs.addFlashAttribute("hasMessage", true);
			redirAttrs.addFlashAttribute("class", "alert-info");
			redirAttrs.addFlashAttribute("message", "栽培計画の編集に成功しました。");
			// リダイレクト先に編集した栽培計画IDのパラメータを渡し、作物詳細画面を表示
			return "redirect:/plans/detail/" + entity.getId();
		}
		// cmdの値が万が一指定されなかった場合の処理
		// 使用可能な区画を、モデルに設定
		model.addAttribute("searchSections", searchSections);
		// アラートメッセージを、モデルに設定
		model.addAttribute("hasMessage", true);
		model.addAttribute("class", "alert-danger");
		model.addAttribute("message", "栽培計画の編集に失敗しました。");
		// 栽培計画編集画面を返す
		return "plans/edit";
	}

	/**
	 * 栽培計画の削除
	 *
	 * @param planId     削除する栽培計画のID
	 * @param model      ビューで使用するモデル
	 * @param redirAttrs リダイレクト属性
	 * @return 栽培計画一覧画面
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	@GetMapping(path = "/plans/delete/{planId}")
	public String delete(@PathVariable Long planId, Model model, RedirectAttributes redirAttrs) throws IOException {
		// 栽培計画IDから栽培計画を削除
		repository.deleteById(planId);
		// アラートメッセージをリダイレクト先のモデルに設定
		redirAttrs.addFlashAttribute("hasMessage", true);
		redirAttrs.addFlashAttribute("class", "alert-info");
		redirAttrs.addFlashAttribute("message", "栽培計画の削除に成功しました。");
		// 栽培計画一覧画面を返す
		return "redirect:/plans/list";
	}

	/**
	 * 栽培計画一覧の表示<br>
	 * 計画中の栽培計画のみを表示します。また、ガントチャート形式でも表示します。
	 *
	 * @param principal 認証情報
	 * @param model     ビューで使用するモデル
	 * @return 栽培計画一覧画面
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	@GetMapping(path = "/plans/list")
	public String showList(Principal principal, Model model) throws IOException {
		// 認証情報からユーザ情報を取得
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		// ユーザIDに対応する栽培計画の中で計画中のものを取得し、リストを生成
		List<Plan> list = repository.findAllByUserIdAndCompletionFalseOrderByUpdatedAtAsc(user.getUserId());
		// 生成したリストからガントチャート表示するための、ガントリストを生成
		List<PlanForm> gantList = getGantList(null, list);
		// 年での絞りこみ検索の際に使用する年の選択リストとして、ユーザーIDから年リストを生成
		Set<Integer> yearList = getYearList(user.getUserId());
		// 区画での絞り込み検索の際に使用する区画の選択リストとして、ユーザーIDから区画リストを生成
		List<Section> sectionList = sectionRepository.findAllByUserIdOrderByUpdatedAtAsc(user.getUserId());
		// リストをモデルに設定
		model.addAttribute("list", list);
		// ガントリストをモデルに設定
		model.addAttribute("gantList", gantList);
		// 年リストをモデルに設定
		model.addAttribute("yearList", yearList);
		// 区画リストをモデルに設定
		model.addAttribute("sectionList", sectionList);
		// 栽培ステイタスでの絞り込み検索のラジオボタン選択の値は"progress"（計画中）を設定
		model.addAttribute("option", "progress");
		// 栽培計画一覧画面を返す
		return "/plans/list";
	}

	/**
	 * 栽培計画の検索<br>
	 * 栽培計画一覧画面から栽培計画検索を行う（作物名、区画名、年、栽培ステイタスでの絞り込み検索機能あり）
	 *
	 * @param principal   認証情報
	 * @param option      検索オプション（値は"progress"、"completion"、nullの３つ）
	 * @param keyword     検索キーワード（作物名）
	 * @param sectionName 区画名
	 * @param year        年
	 * @param model       ビューで使用するモデル
	 * @param redirAttrs  リダイレクト属性
	 * @return 栽培計画一覧画面
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	@PostMapping(path = "/plans/search")
	public String searchCrops(Principal principal, @RequestParam(name = "option", required = false) String option,
			@RequestParam(name = "keyword", required = false) String keyword,
			@RequestParam(name = "sectionName", required = false) String sectionName,
			@RequestParam(name = "year", required = false) Integer year, Model model, RedirectAttributes redirAttrs)
			throws IOException {
		// 認証情報からユーザ情報を取得
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		// 区画での絞り込み検索の際に使用する区画の選択リストとして、ユーザーIDから区画リストを生成
		List<Section> sectionList = sectionRepository.findAllByUserIdOrderByUpdatedAtAsc(user.getUserId());
		// 検索結果として表示するための栽培計画のリストを初期化
		List<Plan> list = new ArrayList<>();
		// 区画名の指定がない場合の処理
		if (sectionName == null || sectionName.isEmpty()) {
			// 検索キーワード（作物名）の指定がない場合の処理
			if (keyword == null) {
				// 検索オプションが栽培計画中の場合、ユーザーに対応する栽培計画中の栽培計画を取得し、リストに代入
				if ("progress".equals(option)) {
					list = repository.findAllByUserIdAndCompletionFalseOrderByUpdatedAtAsc(user.getUserId());
					// 検索オプションが栽培完了の場合、ユーザーに対応する栽培計画が完了した栽培計画を取得し、リストに代入
				} else if ("completion".equals(option)) {
					list = repository.findAllByUserIdAndCompletionTrueOrderByUpdatedAtAsc(user.getUserId());
					// 検索オプションがが指定されていない場合、ユーザーに対応する全ての栽培計画を取得し、リストに代入
				} else {
					list = repository.findAllByUserIdOrderByUpdatedAtAsc(user.getUserId());
				}
			}
			// 検索キーワード（作物名）の指定がある場合の処理
			if (keyword != null) {
				// 検索オプションが栽培計画中の場合、ユーザーに対応する栽培計画中の栽培計画の内、検索キーワードを作物名に含むものを取得し、リストに代入
				if ("progress".equals(option)) {
					list = repository.findAllByUserIdAndCompletionFalseAndCropNameContainingOrderByUpdatedAtAsc(
							user.getUserId(), keyword);
					// 検索オプションが栽培完了の場合、ユーザーに対応する栽培計画が完了した栽培計画の内、検索キーワードを作物名に含むものを取得し、リストに代入
				} else if ("completion".equals(option)) {
					list = repository.findAllByUserIdAndCompletionTrueAndCropNameContainingOrderByUpdatedAtAsc(
							user.getUserId(), keyword);
					// 検索オプションがが指定されていない場合、ユーザーに対応する全ての栽培計画の内、検索キーワードを作物名に含むものを取得し、リストに代入
				} else {
					list = repository.findAllByUserIdAndCropNameContainingOrderByUpdatedAtAsc(user.getUserId(),
							keyword);
				}
			}
			// 区画名の指定がある場合の処理
		} else if (sectionName != null) {
			// 検索キーワード（作物名）の指定がない場合の処理
			if (keyword == null) {
				// 検索オプションが栽培計画中の場合、ユーザーに対応する栽培計画中の栽培計画の内、指定された区画名の区画を使用しているものを取得し、リストに代入
				if ("progress".equals(option)) {
					list = repository.findAllByUserIdAndCompletionFalseAndSectionNameOrderByUpdatedAtAsc(
							user.getUserId(), sectionName);
					// 検索オプションが栽培完了の場合、ユーザーに対応する栽培計画が完了した栽培計画の内、指定された区画名の区画を使用しているものを取得し、リストに代入
				} else if ("completion".equals(option)) {
					list = repository.findAllByUserIdAndCompletionTrueAndSectionNameOrderByUpdatedAtAsc(
							user.getUserId(), sectionName);
					// 検索オプションがが指定されていない場合、ユーザーに対応する全ての栽培計画の内、指定された区画名の区画を使用しているものを取得し、リストに代入
				} else {
					list = repository.findAllByUserIdAndSectionNameOrderByUpdatedAtAsc(user.getUserId(), sectionName);
				}
			}
			// 検索キーワード（作物名）の指定がある場合の処理
			if (keyword != null) {
				// 検索オプションが栽培計画中の場合、ユーザーに対応する栽培計画中の栽培計画の内、検索キーワードを作物名に含み、指定された区画名の区画を使用しているものを取得し、リストに代入
				if ("progress".equals(option)) {
					list = repository
							.findAllByUserIdAndCompletionFalseAndCropNameContainingAndSectionNameOrderByUpdatedAtAsc(
									user.getUserId(), keyword, sectionName);
					// 検索オプションが栽培完了の場合、ユーザーに対応する栽培計画が完了した栽培計画の内、検索キーワードを作物名に含み、指定された区画名の区画を使用しているものを取得し、リストに代入
				} else if ("completion".equals(option)) {
					list = repository
							.findAllByUserIdAndCompletionTrueAndCropNameContainingAndSectionNameOrderByUpdatedAtAsc(
									user.getUserId(), keyword, sectionName);
					// 検索オプションがが指定されていない場合、ユーザーに対応する全ての栽培計画の内、検索キーワードを作物名に含み、指定された区画名の区画のものを取得し、リストに代入
				} else {
					list = repository.findAllByUserIdAndCropNameContainingAndSectionNameOrderByUpdatedAtAsc(
							user.getUserId(), keyword, sectionName);
				}
			}
		}
		// 年の指定がある場合の処理
		if (year != null) {
			// 生成したリストから、指定の年に栽培期間が含まれる栽培計画を取得し、再度リストに代入
			list = getListOfYear(year, list);
		}
		// 検索結果として返すリストが空の場合、モデルにメッセージを設定
		if (list.isEmpty()) {
			model.addAttribute("message", "その栽培計画は見つかりませんでした。");
		}
		// 年での絞りこみ検索の際に使用する年の選択リストとして、ユーザーIDから年リストを生成
		Set<Integer> yearList = getYearList(user.getUserId());
		// 生成したリストからガントチャート表示するための、ガントリストを生成
		List<PlanForm> gantList = getGantList(year, list);
		// リストをモデルに設定
		model.addAttribute("list", list);
		// ガントチャートリストをモデルに設定
		model.addAttribute("gantList", gantList);
		// 年リストをモデルに設定
		model.addAttribute("yearList", yearList);
		// 区画リストをモデルに設定
		model.addAttribute("sectionList", sectionList);
		// 送られてきたパラメータの値を、それぞれモデルに設定
		model.addAttribute("sectionName", sectionName);
		model.addAttribute("keyword", keyword);
		model.addAttribute("option", option);
		model.addAttribute("year", year);
		// 栽培計画一覧画面を返す
		return "/plans/list";
	}

	/**
	 * 使用可能な区画のリストを生成するメソッド
	 *
	 * @param userId                  ユーザID
	 * @param sowing_date             播種日
	 * @param harvest_completion_date 収穫完了予定日
	 * @param planId                  栽培計画ID
	 * @return 使用可能な区画のリスト
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	private List<Section> availableSctionGenerater(Long userId, LocalDate sowing_date,
			LocalDate harvest_completion_date, Long planId) throws IOException {
		// ユーザーに対応する全ての区画を取得し、区画リストを生成
		List<Section> sectionList = sectionRepository.findAllByUserIdOrderByUpdatedAtAsc(userId);
		// 検索結果として返す為の、使用可能な区画のリストを初期化
		List<Section> searchSections = new ArrayList<>();
		// 生成した区画リストの全ての区画に対して行う処理
		outerLoop: for (Section section : sectionList) {
			// 区画に対応する栽培計画のリストを生成
			List<Plan> planList = section.getPlans();
			// 栽培計画ＩＤを持つ栽培計画が既に存在している場合、つまり栽培計画の編集時の処理
			if (planId != null) {
				// 栽培計画ＩＤから栽培計画を取得
				Optional<Plan> optionalPlan = repository.findById(planId);
				Plan thisPlan = optionalPlan.orElseThrow(() -> new RuntimeException("Plan not found"));
				// 生成した区画に対応する栽培計画リストに取得した栽培計画が含まれている場合は、その栽培計画をリストから削除
				if (planList.contains(thisPlan)) {
					planList.remove(thisPlan);
				}
			}
			// 生成した区画に対応する栽培計画リストの全ての栽培計画に対して行う処理
			for (Plan plan : planList) {
				// 栽培計画の播種日を取得
				LocalDate otherPlanSowing_date = plan.getSowing_date();
				// 栽培計画の収穫完了予定日を取得
				LocalDate otherPlanHarvest_completion_date = plan.getHarvest_completion_date();
				// 栽培計画の栽培期間が、パラメータで送信された栽培計画の栽培期間と被る場合の処理
				if (!(otherPlanHarvest_completion_date.isBefore(sowing_date)
						|| otherPlanSowing_date.isAfter(harvest_completion_date))) {
					// 当区画の判定を終了
					continue outerLoop;
				}
			}
			// 上記の検証を通過した区画を使用可能な区画のリストに追加
			searchSections.add(section);
		}
		// 使用可能な区画のリストを返す
		return searchSections;
	}

	/**
	 * 栽培計画エンティティから栽培計画フォームオブジェクトを生成するメソッド
	 *
	 * @param plan 栽培計画エンティティ
	 * @return 栽培計画フォームオブジェクト
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	public PlanForm getPlanForm(Plan plan) throws IOException {
		// ModelMapperの設定: 重複があっても無視する
		modelMapper.getConfiguration().setAmbiguityIgnored(true);
		// 栽培計画クラスから栽培計画フォームクラスへのマッピングの設定（栽培計画フォームの作物名の設定はスキップ）
		modelMapper.typeMap(Plan.class, PlanForm.class).addMappings(mapper -> mapper.skip(PlanForm::setCropName));
		// 栽培計画クラスから栽培計画フォームクラスへのマッピングの設定（栽培計画フォームの区画名の設定はスキップ）
		modelMapper.typeMap(Plan.class, PlanForm.class).addMappings(mapper -> mapper.skip(PlanForm::setSectionName));
		// 栽培計画エンティティから栽培計画フォームオブジェクトへの変換
		PlanForm form = modelMapper.map(plan, PlanForm.class);
		// 栽培計画が紐づく作物から作物名を取得
		String cropName = plan.getCrop().getName();
		// 栽培計画が紐づく区画から区画名を取得
		String sectionName = plan.getSection().getName();
		// 栽培計画フォームオブジェクトに取得した作物名と区画名を設定
		form.setCropName(cropName);
		form.setSectionName(sectionName);
		// 生成した栽培計画フォームオブジェクトを返す
		return form;
	}

	/**
	 * 年と栽培計画リストからガントチャート表示用の栽培計画フォームオブジェクトを生成するメソッド<br>
	 * 栽培計画リストをガントチャートにそのまま使用しない理由は、ガントチャートがLong型の作物IDと区画IDの値を読み込めずエラーになってしまう為。
	 * 
	 * @param year 年の指定
	 * @param list 栽培計画リスト
	 * @return ガントチャート表示用の栽培計画フォームリスト
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	private List<PlanForm> getGantList(Integer year, List<Plan> list) throws IOException {
		// ガントチャートで表示する栽培計画フォームオブジェクトのリストとしてガントリストを初期化
		List<PlanForm> gantList = new ArrayList<>();
		// 栽培計画リストの全ての栽培計画に対して行う処理
		for (Plan plan : list) {
			// 栽培計画を栽培計画フォームに変換
			PlanForm planForm = getPlanForm(plan);
			// 生成したガントリストに、変換した栽培計画フォームを追加
			gantList.add(planForm);
		}
		// yearの指定がある場合、ガントチャートのX軸の表示幅を一年間に指定する為、ダミーのオブジェクトを一つ生成する処理
		// 年の指定がある場合の処理
		if (year != null) {
			// 指定された年の最初の日付を取得
			LocalDate startDate = LocalDate.ofYearDay(year, 1);
			// 指定された年がうるう年である場合は366日を、そうでない場合は365日を年の日数に代入
			int dayOfYear = Year.of(year).isLeap() ? 366 : 365;
			// 指定された年と取得した年の日数から、年の最後の日付を取得
			LocalDate endDate = LocalDate.ofYearDay(year, dayOfYear);
			// ガントチャートのX軸の表示幅を一年間に指定する為のダミーのオブジェクトであるスタートエンドに、取得した年の最初の日付と最後の日付を設定し、生成
			PlanForm start_end = new PlanForm((long) 0, "start_end", "", startDate, endDate, true);
			// スタートエンドをガントリストに追加
			gantList.add(start_end);

			// ガントリスト内の栽培計画の栽培期間が年を跨ぐ場合に指定の年からはみ出た部分をカットする処理
			// ガントリストの全ての栽培計画フォームオブジェクトに対して行う処理
			for (PlanForm planForm : gantList) {
				// 栽培計画の播種日が、指定された年の最初の日よりも前の場合は、栽培計画の播種日を指定された年の最初の日に設定
				if (planForm.getSowing_date().isBefore(startDate)) {
					planForm.setSowing_date(startDate);
				}
				// 栽培計画の収穫完了日が、指定された年の最後の日よりも後の場合は、栽培計画の収穫完了予定日を指定された年の最後の日に設定
				if (planForm.getHarvest_completion_date().isAfter(endDate)) {
					planForm.setHarvest_completion_date(endDate);
				}
			}
		}
		// 生成したガントリストを返す
		return gantList;

	}

	/**
	 * 栽培計画の年での絞り込み検索で、年の選択リストに使用するための年のリストをユーザーIDから取得するメソッド
	 *
	 * @param userId ユーザーID
	 * @return 年のリスト
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	private Set<Integer> getYearList(Long userId) throws IOException {
		// 年リストを生成 ※重複を許可しないHashSet型
		Set<Integer> yearList = new HashSet<>();
		// ユーザーIDから、ユーザーに対応する栽培計画のリストを生成
		List<Plan> list = repository.findAllByUserIdOrderByUpdatedAtAsc(userId);
		// 栽培計画のリストの全ての栽培計画に対して行う処理
		for (Plan plan : list) {
			// 栽培計画の播種日と収穫完了予定日からその日付の年を取得し、生成した年リストに追加
			yearList.add(plan.getSowing_date().getYear());
			yearList.add(plan.getHarvest_completion_date().getYear());
		}
		// 生成した年リストを返す
		return yearList;
	}

	/**
	 * 栽培計画リストから、指定された年に栽培期間が含まれる計画のみを抽出して返しすメソッド
	 *
	 * @param year 年の指定
	 * @param list 抽出対象の栽培計画リスト
	 * @return 抽出後の栽培計画リスト
	 */
	private List<Plan> getListOfYear(Integer year, List<Plan> list) {
		// 栽培計画リストに含まれる栽培計画の播種日または収穫完了予定日の年が指定の年でない場合、栽培計画リストから除外
		list.removeIf(plan -> !(plan.getSowing_date().getYear() == year
				|| plan.getHarvest_completion_date().getYear() == year));
		// 抽出後の栽培計画リストを返す
		return list;
	}

}
