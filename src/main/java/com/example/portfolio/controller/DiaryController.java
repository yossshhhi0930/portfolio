package com.example.portfolio.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import com.example.portfolio.repository.DiaryImageReposiory;
import com.example.portfolio.repository.DiaryRepository;
import com.example.portfolio.repository.PlanRepository;
import com.example.portfolio.entity.Diary;
import com.example.portfolio.entity.DiaryImage;
import com.example.portfolio.entity.UserInf;
import com.example.portfolio.form.DiaryForm;

/**
 * 栽培日誌に関する操作（登録、編集、表示、削除）を担当するコントローラークラス
 */
@Controller

public class DiaryController {

	// ロガーの初期化
	protected static Logger log = LoggerFactory.getLogger(DiaryController.class);

	// ModelMapperの注入
	@Autowired
	private ModelMapper modelMapper;

	// 栽培日誌リポジトリの注入
	@Autowired
	DiaryRepository repository;

	// 栽培日誌画像リポジトリの注入
	@Autowired
	DiaryImageReposiory imageRepository;

	// 栽培計画リポジトリの注入
	@Autowired
	PlanRepository planRepository;

	// 栽培日誌画像の保存先ディレクトリのパス
	@Value("${upload.path}")
	private String UPLOAD_DIR;

	// 栽培日誌画像エンティティにセットするパス
	@Value("${set.path}")
	private String SET_PATH;

	/**
	 * 栽培日誌登録画面の表示<br>
	 *
	 * @param planId 登録する栽培日誌が紐づく栽培計画のID
	 * @param model  ビューで使用するモデル
	 * @return 栽培日誌登録画面
	 */
	@GetMapping(path = "/diarys/new/{planId}")
	public String newDiary(@PathVariable Long planId, Model model) {
		// 栽培日誌フォームを初期化
		DiaryForm form = new DiaryForm();
		// 栽培計画のIDを初期化した栽培日誌フォームに設定
		form.setPlanId(planId);
		// 栽培日誌フォームをモデルに設定
		model.addAttribute("form", form);
		// 栽培日誌登録画面を返す
		return "diarys/new";
	}

	/**
	 * 栽培日誌の登録<br>
	 * 栽培日誌登録画面から送信されたフォームデータ及び画像データの検証、保存を行う
	 *
	 * @param principal  認証情報
	 * @param images     栽培日誌画像の配列
	 * @param form       栽培日誌登録フォームデータ
	 * @param result     フォーム検証の結果
	 * @param model      ビューで使用するモデル
	 * @param redirAttrs リダイレクト属性
	 * @return 検証結果に基づく画面
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	@PostMapping(path = "/diary")
	public String create(Principal principal, @RequestParam("images") MultipartFile[] images,
			@Validated @ModelAttribute("form") DiaryForm form, BindingResult result, Model model,
			RedirectAttributes redirAttrs) throws IOException {
		// 認証情報からユーザーを取得
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		// フォームデータを変数に代入
		Long userId = user.getUserId();
		Long planId = form.getPlanId();
		LocalDate record_date = form.getRecord_date();
		String description = form.getDescription();
		// 画像に関するエラーカウンターの初期化
		int imageErrorCount = 0;
		// フォームのフィールドエラー以外のエラー（今回は画像に関するエラー）を表示するための、メッセージリストを初期化
		List<String> messagelist = new ArrayList<>();
		// 栽培日誌画像が1枚以上選択されており、画像ファイルの拡張子が画像形式でない場合に返すエラーの追加
		// 栽培日誌画像の配列の中身が存在しているかの検証
		if (images != null && images.length > 0 && !images[0].isEmpty()) {
			// その他画像の配列に存在している全ての画像に対して行う処理
			for (MultipartFile image : images) {
				// 栽培日誌画像がnullでなく、画像ファイルの拡張子が画像形式でない場合の処理
				if (image != null && !isImageFile(image)) {
					// 画像エラーメッセージをモデルに設定
					model.addAttribute("imageError", "画像ファイル形式が正しくありません。");
					// メッセージリストにエラーメッセージを追加
					messagelist.add("画像ファイル形式が正しくありません。");
					// 画像に関するエラーカウンターの更新
					imageErrorCount++;
				}
			}
		}
		// 栽培計画のIDが未入力、またはIDが誤っている場合に返すエラーの追加
		if (planId == null || planRepository.findById(planId) == null) {
			FieldError fieldError = new FieldError(result.getObjectName(), "planId", "栽培計画が存在しません。");
			result.addError(fieldError);
		}
		// エラーがあった場合に、栽培日誌登録画面を返し、エラーメッセージを表示
		if (imageErrorCount > 0 || result.hasErrors()) {
			// メッセージリストをモデルに設定
			model.addAttribute("notFieldMessages", messagelist);
			// アラートメッセージをモデルに設定
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-danger");
			model.addAttribute("message", "栽培日誌の登録に失敗しました。");
			// 栽培日誌登録画面を返す
			return "diarys/new";
		}
		// エラーが無かった場合の処理
		// フォームデータで、栽培日誌エンティティを生成
		Diary entity = new Diary(userId, planId, record_date, description);
		// 栽培日誌エンティティを保存
		repository.saveAndFlush(entity);
		// 栽培日誌画像の登録
		// 一意の画像ファイル名を作成する為に、ファイル名に付ける日付文字列を生成
		String formattedDateTime = getFormattedDateTime();
		// 栽培日誌画像エンティティに設定するアップロードパスを生成
		Path uploadPath = Path.of(SET_PATH);
		// 栽培日誌画像が１枚以上選択されている場合に行う、栽培日誌画像の保存、エンティティの登録処理
		// 栽培日誌画像の配列の中身が存在しているかの検証
		if (images != null && images.length > 0 && !images[0].isEmpty()) {
			// 栽培日誌画像の配列に存在している全ての画像に対して行う処理
			for (MultipartFile image : images) {
				// 日付文字列、画像の元のファイル名、生成したアップロードパスから、ファイルパスを生成
				Path filePath = uploadPath.resolve(formattedDateTime + image.getOriginalFilename());
				// 栽培日誌エンティティから栽培日誌IDを取得
				Long diaryId = entity.getId();
				// 生成したファイルパスから画像が表示されるための適切なパス文字列を生成
				String path = StringUtils.cleanPath("/" + filePath.toString());
				// 栽培日誌画像エンティティを生成
				DiaryImage imageEntiy = new DiaryImage(diaryId, path);
				// 栽培日誌画像エンティティを保存
				imageRepository.saveAndFlush(imageEntiy);
				// 画像の保存
				saveFile(image, formattedDateTime);
			}
		}
		// アラートメッセージをリダイレクト先モデルに設定
		redirAttrs.addFlashAttribute("hasMessage", true);
		redirAttrs.addFlashAttribute("class", "alert-info");
		redirAttrs.addFlashAttribute("message", "栽培日誌の登録が完了しました。");
		// リダイレクト先に、登録した栽培日誌IDのパラメータを渡し、栽培日誌詳細画面を表示
		return "redirect:/diarys/detail/" + entity.getId();
	}

	/**
	 * 栽培日誌詳細画面を表示
	 *
	 * @param diaryId 表示する栽培日誌のID
	 * @param model   ビューで使用するモデル
	 * @return 栽培日誌詳細画面
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	@GetMapping(path = "/diarys/detail/{diaryId}")
	public String showDetail(@PathVariable Long diaryId, Model model) throws IOException {
		// 栽培日誌IDから栽培日誌を取得
		Optional<Diary> optionalDiary = repository.findById(diaryId);
		Diary entity = optionalDiary.orElseThrow(() -> new RuntimeException("Diary not found"));
		// 栽培日誌に紐づく全ての栽培日誌画像を表示する為の画像リストを生成
		List<DiaryImage> imageList = imageRepository.findAllByDiaryIdOrderByUpdatedAtAsc(diaryId);
		// 取得した栽培日誌情報をモデルに設定
		model.addAttribute("diary", entity);
		// 生成した画像リストをモデルに設定
		model.addAttribute("list", imageList);
		// 栽培日誌詳細画面を返す
		return "diarys/detail";
	}

	/**
	 * 栽培日誌編集画面の表示<br>
	 *
	 * @param diaryId 編集する栽培日誌のID
	 * @param model   ビューで使用するモデル
	 * @return 栽培日誌編集画面
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	@GetMapping(path = "/diarys/edit/{diaryId}")
	public String showEditPage(@PathVariable Long diaryId, Model model) throws IOException {
		// 栽培日誌IDから栽培日誌を取得
		Optional<Diary> optionalDiary = repository.findById(diaryId);
		Diary entity = optionalDiary.orElseThrow(() -> new RuntimeException("Diary not found"));
		// 栽培日誌から栽培日誌フォームを生成
		DiaryForm form = getDiaryForm(entity);
		// 栽培日誌に紐づく全ての栽培日誌画像を表示する為の画像リストを生成
		List<DiaryImage> imageList = imageRepository.findAllByDiaryIdOrderByUpdatedAtAsc(diaryId);
		// 生成した栽培日誌フォームをモデルに設定
		model.addAttribute("form", form);
		// 生成した画像リストをモデルに設定
		model.addAttribute("list", imageList);
		// 取得した栽培日誌エンティティの栽培計画IDを、モデルに設定
		model.addAttribute("planId", entity.getPlanId());
		// 栽培日誌編集画面を返す
		return "diarys/edit";
	}

	/**
	 * 栽培日誌画像の削除<br>
	 * 栽培日誌編集画面にて、登録済みの栽培日誌画像を削除する際に送られる
	 *
	 * @param imageId    削除する作物日誌画像のID
	 * @param diaryId    栽培日誌画像が紐づく栽培日誌ID
	 * @param model      ビューで使用するモデル
	 * @param attributes リダイレクト属性
	 * @return 栽培日誌編集画面
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	@GetMapping("/diarys/delete-image")
	public String deleteImage(@RequestParam Long imageId, @RequestParam Long diaryId, Model model,
			RedirectAttributes redirAttrs) throws IOException {
		// 栽培日誌画像IDから栽培画像を削除
		imageRepository.deleteById(imageId);
		// アラートメッセージをリダイレクト先のモデルに設定
		redirAttrs.addFlashAttribute("hasMessage", true);
		redirAttrs.addFlashAttribute("class", "alert-info");
		redirAttrs.addFlashAttribute("message", "画像の削除が完了しました。");
		// リダイレクト先に削除した栽培日誌画像が紐づく栽培日誌IDのパラメータを渡し、栽培日誌編集画面を表示
		return "redirect:/diarys/edit/" + diaryId;
	}

	/**
	 * 栽培日誌の編集<br>
	 * 栽培日誌編集画面から送信されたフォームデータ及び画像データの検証、更新を行う
	 *
	 * @param images     栽培日誌画像の配列
	 * @param form       栽培日誌編集フォームデータ
	 * @param result     フォーム検証の結果
	 * @param model      ビューで使用するモデル
	 * @param redirAttrs リダイレクト属性
	 * @return 検証結果に基づく画面
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	@PostMapping(path = "/diarys/edit-complete")
	public String edit(@RequestParam("images") MultipartFile[] images,
			@Validated @ModelAttribute("form") DiaryForm form, BindingResult result, Model model,
			RedirectAttributes redirAttrs) throws IOException {
		// フォームデータを変数に代入
		Long planId = form.getPlanId();
		LocalDate record_date = form.getRecord_date();
		String description = form.getDescription();
		Optional<Diary> optionalDiary = repository.findById(form.getId());
		Diary entity = optionalDiary.orElseThrow(() -> new RuntimeException("Diary not found"));
		// フォームのフィールドエラー以外のエラー（今回は画像に関するエラー）を表示するための、メッセージリストを初期化
		List<String> messagelist = new ArrayList<>();
		// 画像に関するエラーカウンターの初期化
		int imageErrorCount = 0;
		// 栽培日誌画像が1枚以上選択されており、画像ファイルの拡張子が画像形式でない場合に返すエラーの追加
		// 栽培日誌画像の配列の中身が存在しているかの検証
		if (images != null && images.length > 0 && !images[0].isEmpty()) {
			// 栽培日誌画像の配列に存在している全ての画像に対して行う処理
			for (MultipartFile image : images) {
				// 栽培日誌画像がnullでなく、画像ファイルの拡張子が画像形式でない場合の処理
				if (image != null && !isImageFile(image)) {
					// エラーメッセージをモデルに設定
					model.addAttribute("imageError", "画像ファイル形式が正しくありません。");
					// メッセージリストにエラーメッセージを追加
					messagelist.add("画像ファイル形式が正しくありません。");
					// 画像に関するエラーカウンターの更新
					imageErrorCount++;
				}
			}
		}
		// 栽培計画のIdが未入力、またはIdが誤っている場合に返すエラーの追加
		if (planId == null || planRepository.findById(planId) == null) {
			FieldError fieldError = new FieldError(result.getObjectName(), "planId", "栽培計画が存在しません。");
			result.addError(fieldError);
		}
		// エラーがあった場合に、栽培日誌登録画面を返し、エラーメッセージを表示
		if (imageErrorCount > 0 || result.hasErrors()) {
			// メッセージリストをモデルに設定
			model.addAttribute("notFieldMessages", messagelist);
			// アラートメッセージをモデルに設定
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-danger");
			model.addAttribute("message", "栽培日誌の編集に失敗しました。");
			// 栽培日誌登録画面を返す
			return "diarys/edit";
		}
		// エラーが無かった場合の処理
		// フォームデータで栽培日誌エンティティを更新
		entity.setPlanId(planId);
		entity.setRecord_date(record_date);
		entity.setDescription(description);
		// 更新された栽培日誌エンティティを保存
		repository.saveAndFlush(entity);
		// 栽培日誌画像の登録
		// 一意の画像ファイル名を作成する為に、ファイル名に付ける日付文字列を生成
		String formattedDateTime = getFormattedDateTime();
		// 画像エンティティに保存するアップロードパスの生成
		Path uploadPath = Path.of(SET_PATH);
		// 栽培日誌画像が１枚以上選択されている場合に行う、栽培日誌画像の保存、エンティティの登録
		// 栽培日誌画像の配列の中身が存在しているかの検証
		if (images != null && images.length > 0 && !images[0].isEmpty()) {
			// 栽培日誌画像の配列に存在している全ての画像に対して行う処理
			for (MultipartFile image : images) {
				// 日付文字列、画像の元のファイル名、生成したアップロードパスから、ファイルパスを生成
				Path filePath = uploadPath.resolve(formattedDateTime + image.getOriginalFilename());
				// 栽培日誌画像エンティティから栽培日誌IDを取得
				Long diaryId = entity.getId();
				// 生成したファイルパスから画像が表示されるための適切なパス文字列を生成
				String path = StringUtils.cleanPath("/" + filePath.toString());
				// 栽培日誌画像エンティティを生成
				DiaryImage imageEntiy = new DiaryImage(diaryId, path);
				// 栽培日誌画像エンティティを保存
				imageRepository.saveAndFlush(imageEntiy);
				// 画像の保存
				saveFile(image, formattedDateTime);
			}
		}
		// アラートメッセージをリダイレクト先のモデルに設定
		redirAttrs.addFlashAttribute("hasMessage", true);
		redirAttrs.addFlashAttribute("class", "alert-info");
		redirAttrs.addFlashAttribute("message", "栽培日誌の編集が完了しました。");
		// リダイレクト先に栽培日誌IDのパラメータを渡し、栽培日誌詳細画面を表示
		return "redirect:/diarys/detail/" + entity.getId();
	}

	/**
	 * 栽培日誌の削除
	 *
	 * @param diaryId    削除する栽培日誌のID
	 * @param model      ビューで使用するモデル
	 * @param redirAttrs リダイレクト属性
	 * @return 栽培日誌一覧画面
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	@GetMapping(path = "/diarys/delete/{diaryId}")
	public String delete(@PathVariable Long diaryId, Model model, RedirectAttributes redirAttrs) throws IOException {
		// 栽培日誌IDから栽培日誌を削除
		repository.deleteById(diaryId);
		// アラートメッセージをリダイレクト先のモデルに設定
		redirAttrs.addFlashAttribute("hasMessage", true);
		redirAttrs.addFlashAttribute("class", "alert-info");
		redirAttrs.addFlashAttribute("message", "栽培日誌の削除が完了しました。");
		// 栽培日誌一覧画面を返す
		return "redirect:/diarys/list";
	}

	/**
	 * 栽培日誌一覧の表示
	 *
	 * @param principal 認証情報
	 * @param model     ビューで使用するモデル
	 * @return 栽培日誌一覧画面
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	@GetMapping(path = "/diarys/list")
	public String showList(Principal principal, Model model) throws IOException {
		// 認証情報からユーザ情報を取得
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		// ユーザIDに対応する栽培日誌のリストを生成
		List<Diary> list = repository.findAllByUserIdOrderByUpdatedAtAsc(user.getUserId());
		// 生成した栽培日誌のリストをモデルに設定
		model.addAttribute("list", list);
		// 栽培日誌一覧画面を返す
		return "/diarys/list";
	}

	/**
	 * 栽培日誌の検索<br>
	 * 栽培日誌一覧画面から栽培日誌検索を行う（期間での絞り込み検索機能あり）
	 *
	 * @param principal  認証情報
	 * @param start_date 検索対象期間の開始日
	 * @param end_date   検索対象期間の終了日
	 * @param model      ビューで使用するモデル
	 * @param redirAttrs リダイレクト属性
	 * @return 栽培日誌一覧画面
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	@PostMapping(path = "/diarys/search")
	public String searchCrops(Principal principal,
			@RequestParam(name = "start_date", required = false) LocalDate start_date,
			@RequestParam(name = "end_date", required = false) LocalDate end_date, Model model,
			RedirectAttributes redirAttrs) throws IOException {
		// 認証情報からユーザ情報を取得
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		// ユーザーに対応する全ての栽培日誌のリストを生成
		List<Diary> list = repository.findAllByUserIdOrderByUpdatedAtAsc(user.getUserId());
		// 検索結果として表示する、抽出後の新しいリストを初期化
		List<Diary> newList = new ArrayList<>();
		// 検索対象期間の開始日の指定があり、検索対象期間の終了日の指定もある場合
		if (start_date != null && end_date != null) {
			// 生成したユーザーに対応する栽培日誌のリストの全ての栽培日誌に対して行う処理
			for (Diary diary : list) {
				// 栽培日誌の記録日が指定の期間に含まれる場合、新しいリストに追加
				if (diary.getRecord_date().isAfter(start_date.minusDays(1))
						&& diary.getRecord_date().isBefore(end_date.plusDays(1))) {
					newList.add(diary);
				}
			}
		}
		// 検索対象期間の開始日の指定があり、検索対象期間の終了日の指定がない場合
		if (start_date != null && end_date == null) {
			for (Diary diary : list) {
				// 栽培日誌の記録日が検索対象期間の開始日（その日を含む）より後である場合、新しいリストに追加
				if (diary.getRecord_date().isAfter(start_date.minusDays(1))) {
					newList.add(diary);
				}
			}
		}
		// 検索対象期間の開始日の指定がなく、検索対象期間の終了日の指定がある場合
		if (start_date == null && end_date != null) {
			for (Diary diary : list) {
				// 栽培日誌の記録日が検索対象期間の終了日（その日を含む）より前である場合、新しいリストに追加
				if (diary.getRecord_date().isBefore(end_date.plusDays(1))) {
					newList.add(diary);
				}
			}
		}
		// 検索対象期間の開始日の指定がなく、検索対象期間の終了日の指定もない場合
		if (start_date == null && end_date == null) {
			// ユーザーに対応する全ての栽培日誌のリストを、新しいリストにそのまま代入
			newList = list;
		}
		// 検索結果として返す新しいリストの中身が空の場合、モデルにメッセージを設定
		if (newList.isEmpty()) {
			model.addAttribute("sarchMessage", "その栽培日誌は見つかりませんでした。");
		}
		// 生成した新しいリストを、モデルに設定
		model.addAttribute("list", newList);
		// パラメータとして送られてきた検索対象期間の開始日をモデルに設定
		model.addAttribute("start_date", start_date);
		// パラメータとして送られてきた検索対象期間の終了日をモデルに設定
		model.addAttribute("end_date", end_date);
		// 栽培日誌一覧画面を返す
		return "/diarys/list";
	}

	/**
	 * 栽培日誌エンティティから栽培日誌フォームオブジェクトを生成するメソッド
	 *
	 * @param plan 栽培日誌エンティティ
	 * @return 栽培日誌フォームオブジェクト
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	public DiaryForm getDiaryForm(Diary diary) throws IOException {
		// ModelMapperの設定: 重複があっても無視する
		modelMapper.getConfiguration().setAmbiguityIgnored(true);
		// 栽培日誌クラスから栽培日誌フォームクラスへのマッピングの設定
		modelMapper.typeMap(Diary.class, DiaryForm.class).addMapping(Diary::getPlanId, DiaryForm::setPlanId);
		// 栽培日誌エンティティから栽培日誌フォームオブジェクトへの変換
		DiaryForm form = modelMapper.map(diary, DiaryForm.class);
		// 生成した栽培日誌フォームオブジェクトを返す
		return form;
	}

	/**
	 * 画像の拡張子が一般的な画像形式かどうかを判定するメソッド
	 * 
	 * @param file 判定対象のMultipartFile
	 * @return 画像ファイルの場合はtrue、それ以外の場合はfalse
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	private boolean isImageFile(MultipartFile file) throws IOException {
		// 画像ファイルの元の名前を取得
		String fileName = file.getOriginalFilename();
		// 元の画像ファイル名がnuｌｌでなく、画像の拡張子が、画像形式であれば、trueを返し、そうでなければfalseを返す
		return fileName != null
				&& (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png"));
	}

	/**
	 * 一意の画像ファイル名を作成する為に、現在の日時を指定された形式でフォーマットした文字列を取得するメソッド
	 * 
	 * @return フォーマットされた日時の文字列
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	private String getFormattedDateTime() throws IOException {
		// 現在の日付から、LocalDateTime型の現在の日時を取得
		LocalDateTime currentDateTime = LocalDateTime.now();
		// フォーマットパターン "yyyyMMddHHmmss" を使用して、日時を文字列に変換するためのフォーマッタを生成
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
		// 生成したフォーマッタでLocalDateTime型の現在の日時を文字列に変換
		String formattedDateTime = currentDateTime.format(formatter);
		// フォーマットされた日時の文字列を返す
		return formattedDateTime;
	}

	/**
	 * 画像の保存
	 *
	 * @param image             保存する画像ファイル
	 * @param formattedDateTime 画像ファイル名に付与する日時のフォーマット
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	private void saveFile(MultipartFile image, String formattedDateTime) throws IOException {
		// 画像の保存先ディレクトリのアップロードパスを取得
		Path uploadPath = Path.of(UPLOAD_DIR);
		// ディレクトリが存在しない場合は作成
		if (!Files.exists(uploadPath)) {
			Files.createDirectories(uploadPath);
		}
		// ファイルを保存
		try (var inputStream = image.getInputStream()) {
			// 画像ファイル名に付与する日時のフォーマットと画像の元の名前と取得したアップロードパスから、ファイルの保存先パスを生成
			Path filePath = uploadPath.resolve(formattedDateTime + image.getOriginalFilename());
			// ファイルをコピーして保存
			Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
		}
	}
}
