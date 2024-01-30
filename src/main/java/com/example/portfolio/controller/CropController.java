package com.example.portfolio.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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
import com.example.portfolio.repository.CropRepository;
import com.example.portfolio.repository.CropImageReposiory;
import com.example.portfolio.entity.Crop;
import com.example.portfolio.entity.CropImage;
import com.example.portfolio.entity.UserInf;
import com.example.portfolio.form.CropForm;

/**
 * 作物に関する操作（登録、編集、表示、削除）を担当するコントローラークラス
 */
@Controller
public class CropController {

	// ロガーの初期化
	protected static Logger log = LoggerFactory.getLogger(CropController.class);

	// ModelMapperの注入
	@Autowired
	private ModelMapper modelMapper;

	// 作物リポジトリの注入
	@Autowired
	CropRepository repository;

	// 作物画像リポジトリの注入
	@Autowired
	CropImageReposiory imageRepository;

	// 作物画像の保存先ディレクトリのパス
	@Value("${upload.path}")
	private String UPLOAD_DIR;

	// 作物画像エンティティにセットするパス
	@Value("${set.path}")
	private String SET_PATH;

	/**
	 * 指定の月の播種可能作物一覧の表示
	 *
	 * @param principal 認証情報
	 * @param month     指定の月
	 * @param model     ビューで使用するモデル
	 * @return 指定の月の播種可能作物一覧画面
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	@GetMapping(path = "/crops")
	public String index(Principal principal, @ModelAttribute("month") String month, Model model) throws IOException {
		// 認証情報からユーザーを取得
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		// 指定の月の播種可能作物一覧を表示するための、作物リストを初期化
		List<Crop> monthCrops = new ArrayList<Crop>();
		// 現在の年を取得
		int year = LocalDate.now().getYear();
		// 取得した現在の年と指定の月からYearMonth型の年月を生成
		YearMonth yearMonth = YearMonth.of(year, Month.valueOf(month));
		// 生成した年月の最初の日付を取得
		LocalDate firstDayOfMonth = yearMonth.atDay(1);
		// 生成した年月の最後の日付を取得
		LocalDate lastDayOfMonth = yearMonth.atEndOfMonth();
		// ユーザーIDから、ユーザーに対応する全ての作物を取得し、全ての作物リストを生成
		List<Crop> allCrops = repository.findAllByUserIdOrderByUpdatedAtAsc(user.getUserId());
		// 取得した全ての作物リストの作物に対し、作物の播種可能期間が生成した年月に含まれていれば作物リストに追加する処理を実行
		for (Crop crop : allCrops) {
			// 作物のDate型の播種可能開始日（年の値はデフォルト値）と取得した現在の年から、LocalDate型の播種可能開始日を生成
			LocalDate sowing_start_localDate = getLocalDate(crop.getSowing_start(), year);
			// 作物のDate型の播種可能終了日（年の値はデフォルト値）と取得した現在の年から、LocalDate型の播種可能終了日を生成
			LocalDate sowing_end_localDate = getLocalDate(crop.getSowing_end(), year);
			// 栽培期間が年を跨ぐ場合の処理
			// LocalDate型の播種可能終了日がLocalDate型の播種可能開始日よりも前の日付となっている場合（播種可能期間は1年以内という前提において、この場合を播種可能期間が年を跨いでいると見なす）
			if (sowing_end_localDate.isBefore(sowing_start_localDate)) {
				// 現在の年から翌年の年を取得
				int nextYear = LocalDate.now().getYear() + 1;
				// LocalDate型の播種可能終了日の年を翌年に変更し再取得
				sowing_end_localDate = getLocalDate(crop.getSowing_end(), nextYear);
				// LocalDate型の播種可能終了日から播種可能終了月を取得
				int sowing_end_month = sowing_end_localDate.getMonthValue();
				// 1月から播種可能終了月までの月は全て来年の月の意味合いになるので、1月から播種可能終了月までの月を指定された場合、その月をを翌年の月にに変更する
				for (int i = 1; i <= sowing_end_month; i++) {
					if (Month.valueOf(month).getValue() == i) {
						// 現在の年と指定の月から取得したYearMonth型の年月を翌年の年月に変更し再取得
						yearMonth = YearMonth.of(nextYear, Month.valueOf(month));
						// 指定の月の最初の日付と最後の日付も翌年に変更し再取得
						firstDayOfMonth = yearMonth.atDay(1);
						lastDayOfMonth = yearMonth.atEndOfMonth();
					}
				}
			}
			// 作物の播種可能期間が生成した年月に含まれていれば作物リストに追加
			if (!(sowing_end_localDate.isBefore(firstDayOfMonth) || sowing_start_localDate.isAfter(lastDayOfMonth))) {
				monthCrops.add(crop);
			}
		}

		// 指定の月名を日本語に変換
		String JapaneseMonth = Month.valueOf(month).getDisplayName(TextStyle.FULL, Locale.JAPANESE);
		// 指定の月の作物リストをモデルに設定
		model.addAttribute("list", monthCrops);
		// 日本語の指定の月名をモデルに設定
		model.addAttribute("month", JapaneseMonth);
		// 指定の月の播種可能作物一覧画面を返す
		return "crops/index";
	}

	/**
	 * 作物登録画面の表示
	 *
	 * @param model ビューで使用するモデル
	 * @return 作物登録画面
	 */
	@GetMapping(path = "/crops/new")
	public String newTopic(Model model) {
		// 作物フォームを初期化
		CropForm form = new CropForm();
		// 初期化した作物フォームをモデルのフォームに設定し、ユーザー登録画面を返す
		model.addAttribute("form", form);
		return "crops/new";
	}

	/**
	 * 作物の登録<br>
	 * 作物登録画面から送信されたフォームデータ及び画像データの検証、保存を行う
	 *
	 * @param principal  認証情報
	 * @param topImage   トップ画像
	 * @param images     その他画像の配列
	 * @param form       作物登録フォームデータ
	 * @param result     フォーム検証の結果
	 * @param model      ビューで使用するモデル
	 * @param redirAttrs リダイレクト属性
	 * @return 検証結果に基づく画面
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	@PostMapping(path = "/crop")
	public String create(Principal principal, @RequestParam("topImage") MultipartFile topImage,
			@RequestParam("images") MultipartFile[] images, @Validated @ModelAttribute("form") CropForm form,
			BindingResult result, Model model, RedirectAttributes redirAttrs) throws IOException {
		// 認証情報からユーザーを取得
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		// フォームデータを変数に代入
		Long userId = user.getUserId();
		String name = form.getName();
		Date sowing_start = form.getSowing_start();
		Date sowing_end = form.getSowing_end();
		int cultivationp_period = form.getCultivationp_period();
		String manual = form.getManual();
		// 画像に関するエラーカウンターの初期化
		int imageErrorCount = 0;
		// フォームのフィールドエラー以外のエラー（今回は画像に関するエラー）を表示するための、メッセージリストを初期化
		List<String> messagelist = new ArrayList<>();
		// トップ画像が選択されており、画像ファイルの拡張子が画像形式でない場合の処理
		if (topImage != null && !topImage.isEmpty() && !isImageFile(topImage)) {
			// トップ画像エラーメッセージをモデルに設定
			model.addAttribute("topImageError", "トップ画像のファイル形式が正しくありません。");
			// メッセージリストにエラーメッセージを追加
			messagelist.add("トップ画像のファイル形式が正しくありません。");
			// 画像に関するエラーカウンターの更新
			imageErrorCount++;
		}
		// その他画像が1枚以上選択されており、画像ファイルの拡張子が画像形式でない場合に返すエラーの追加
		// その他画像の配列の中身が存在しているかの検証
		if (images != null && images.length > 0 && !images[0].isEmpty()) {
			// その他画像の配列に存在している全ての画像に対して行う処理
			for (MultipartFile image : images) {
				// その他画像がnullでなく、画像ファイルの拡張子が画像形式でない場合の処理
				if (image != null && !isImageFile(image)) {
					// その他画像エラーメッセージをモデルに設定
					model.addAttribute("imageError", "その他画像のファイル形式が正しくありません。");
					// メッセージリストにエラーメッセージを追加
					messagelist.add("その他画像のファイル形式が正しくありません。");
					// 画像に関するエラーカウンターの更新
					imageErrorCount++;
				}
			}
		}
		// ユーザーが登録している作物の中に、同一の作物が既に存在する場合に返すエラーの追加
		if (repository.findByNameAndUserId(name, userId) != null) {
			FieldError fieldError = new FieldError(result.getObjectName(), "cropName", "その作物は既に登録されています。");
			result.addError(fieldError);
		}
		// エラーがあった場合に、作物登録画面を返し、エラーメッセージを表示
		if (imageErrorCount > 0 || result.hasErrors()) {
			// メッセージリストをモデルに設定
			model.addAttribute("notFieldMessages", messagelist);
			// アラートメッセージをモデルに設定
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-danger");
			model.addAttribute("message", "作物の登録に失敗しました。");
			// 作物登録画面を返す
			return "crops/new";
		}

		// エラーが無かった場合の処理
		// フォームデータで、作物エンティティを生成
		Crop entity = new Crop(userId, name, sowing_start, sowing_end, cultivationp_period, manual);
		// 作物エンティティを保存
		repository.saveAndFlush(entity);
		// トップ画像、その他画像の登録
		// 一意の画像ファイル名を作成する為に、ファイル名に付ける日付文字列を生成
		String formattedDateTime = getFormattedDateTime();
		// 画像エンティティに設定するアップロードパスを生成
		Path uploadPath = Path.of(SET_PATH);
		// トップ画像が選択されている場合に行う、トップ画像の保存、エンティティの登録
		if (topImage != null && !topImage.isEmpty()) {
			// 日付文字列、画像の元のファイル名、生成したアップロードパスから、ファイルパスを生成
			Path filePath = uploadPath.resolve(formattedDateTime + topImage.getOriginalFilename());
			// 作物エンティティから作物IDを取得
			Long cropId = entity.getId();
			// 生成したファイルパスから画像が表示されるための適切なパス文字列を生成
			String path = StringUtils.cleanPath("/" + filePath.toString());
			// 作物画像エンティティを生成（トップ画像なので、topImageの値を”true”に設定）
			CropImage imageEntiy = new CropImage(cropId, path, true);
			// 作物画像エンティティを保存
			imageRepository.saveAndFlush(imageEntiy);
			// 画像の保存
			saveFile(topImage, formattedDateTime);
		}
		// その他画像が１枚以上選択されている場合に行う、その他画像の保存、エンティティの登録
		// その他画像の配列の中身が存在しているかの検証
		if (images != null && images.length > 0 && !images[0].isEmpty()) {
			// その他画像の配列に存在している全ての画像に対して行う処理
			for (MultipartFile image : images) {
				// 日付文字列、画像の元のファイル名、生成したアップロードパスから、ファイルパスを生成
				Path filePath = uploadPath.resolve(formattedDateTime + image.getOriginalFilename());
				// 作物エンティティから作物IDを取得
				Long cropId = entity.getId();
				// 生成したファイルパスから画像が表示されるための適切なパス文字列を生成
				String path = StringUtils.cleanPath("/" + filePath.toString());
				// 作物画像エンティティを生成（その他画像なので、topImageの値を”false”に設定）
				CropImage imageEntiy = new CropImage(cropId, path, false);
				// 作物画像エンティティを保存
				imageRepository.saveAndFlush(imageEntiy);
				// 画像の保存
				saveFile(image, formattedDateTime);
			}
		}
		// アラートメッセージをリダイレクト先モデルに設定
		redirAttrs.addFlashAttribute("hasMessage", true);
		redirAttrs.addFlashAttribute("class", "alert-info");
		redirAttrs.addFlashAttribute("message", "作物の登録が完了しました。");
		// リダイレクト先に登録した作物IDのパラメータを渡し、作物詳細画面を返す
		return "redirect:/crops/detail/" + entity.getId();
	}

	/**
	 * 作物詳細画面を表示
	 *
	 * @param cropId 表示する作物のID
	 * @param model  ビューで使用するモデル
	 * @return 作物詳細画面
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	@GetMapping(path = "/crops/detail/{cropId}")
	public String showDetail(@PathVariable Long cropId, Model model) throws IOException {
		// 作物IDから作物を取得
		Optional<Crop> optionalCrop = repository.findById(cropId);
		Crop entity = optionalCrop.orElseThrow(() -> new RuntimeException("Crop not found"));
		// 作物に紐づく全ての作物画像を表示する為の画像リストを生成
		List<CropImage> imageList = imageRepository.findAllByCropIdOrderByUpdatedAtAsc(cropId);
		// 取得した作物のデータと作物画像リストをモデルに設定
		model.addAttribute("crop", entity);
		model.addAttribute("list", imageList);
		// 作物詳細画面を返す
		return "crops/detail";
	}

	/**
	 * 作物画像編集画面を表示
	 *
	 * @param cropId 編集する作物のID
	 * @param model  ビューで使用するモデル
	 * @return 作物画像編集画面
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	@GetMapping(path = "/crops/edit-image/{cropId}")
	public String showImageEditPage(@PathVariable Long cropId, Model model) throws IOException {
		// 作物IDから作物を取得
		Optional<Crop> optionalCrop = repository.findById(cropId);
		Crop entity = optionalCrop.orElseThrow(() -> new RuntimeException("Crop not found"));
		// 作物から作物フォーム（表示用）を生成
		CropForm form = getCropForm(entity);
		// 作物に紐づく全ての作物画像を表示する為の画像リストを生成
		List<CropImage> imageList = imageRepository.findAllByCropIdOrderByUpdatedAtAsc(cropId);
		// 取得した作物フォーム(表示用）と作物画像リストをモデルに設定
		model.addAttribute("form", form);
		model.addAttribute("list", imageList);
		// 作物画像編集画面を返す
		return "crops/editImage";
	}

	/**
	 * 作物画像の編集<br>
	 * 作物画像編集画面から送信された画像データの検証、更新を行う
	 *
	 * @param principal  認証情報
	 * @param topImage   トップ画像
	 * @param images     その他画像の配列
	 * @param form       作物フォームデータ
	 * @param model      ビューで使用するモデル
	 * @param redirAttrs リダイレクト属性
	 * @return 検証結果に基づく画面
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	@PostMapping(path = "/crops/edit-image-complete")
	public String editImage(Principal principal, @RequestParam("topImage") MultipartFile topImage,
			@RequestParam("images") MultipartFile[] images, @ModelAttribute("form") CropForm form, Model model,
			RedirectAttributes redirAttrs) throws IOException {
		// 作物IDから作物を取得
		Optional<Crop> optionalCrop = repository.findById(form.getId());
		Crop entity = optionalCrop.orElseThrow(() -> new RuntimeException("Crop not found"));
		// 作物フォームを表示するインプットタグは作物ID以外は、”disabled”を設定しており、値の取得ができない為、作物エンティティから新しく作物フォームを生成
		CropForm newform = getCropForm(entity);
		// 作物に紐づく全ての作物画像を表示する為の画像リストを生成
		List<CropImage> imageList = imageRepository.findAllByCropIdOrderByUpdatedAtAsc(form.getId());
		// 画像に関するエラーカウンターの初期化
		int imageErrorCount = 0;
		// フォームのフィールドエラー以外のエラー（今回は画像に関するエラー）を表示するための、メッセージリストを初期化
		List<String> messagelist = new ArrayList<>();
		// トップ画像が選択されており、トップ画像の登録が既に登録済みである場合に返すエラーの追加
		if (topImage != null && !topImage.isEmpty()
				&& imageRepository.findByCropIdAndTopImageTrue(form.getId()).size() > 0) {
			// トップ画像エラーメッセージをモデルにセット
			model.addAttribute("topImageError", "トップ画像の登録は1itemまでです。");
			// メッセージリストにエラーメッセージを追加
			messagelist.add("トップ画像の登録は1itemまでです。");
			// 画像に関するエラーカウンターの更新
			imageErrorCount++;
		}
		// 上記以外で、トップ画像が選択されており、トップ画像ファイルの拡張子が画像形式でない場合に返すエラーの追加
		else if (topImage != null && !topImage.isEmpty() && !isImageFile(topImage)) {
			// トップ画像エラーメッセージをモデルにセット
			model.addAttribute("topImageError", "トップ画像のファイル形式が正しくありません。");
			// メッセージリストにエラーメッセージを追加
			messagelist.add("トップ画像のファイル形式が正しくありません。");
			// 画像に関するエラーカウンターの更新
			imageErrorCount++;
		}
		// その他画像が1枚以上選択されており、画像ファイルの拡張子が画像形式でない場合に返すエラーの追加
		// その他画像の配列の中身が存在しているかの検証
		if (images != null && images.length > 0 && !images[0].isEmpty()) {
			// その他画像の配列に存在している全ての画像に対して行う処理
			for (MultipartFile image : images) {
				// その他画像がnullでなく、画像ファイルの拡張子が画像形式でない場合の処理
				if (image != null && !isImageFile(image)) {
					// その他画像エラーメッセージをモデルにセット
					model.addAttribute("imageError", "その他画像のファイル形式が正しくありません。");
					// メッセージリストにエラーメッセージを追加
					messagelist.add("その他画像のファイル形式が正しくありません。");
					// 画像に関するエラーカウンターの更新
					imageErrorCount++;
				}
			}
		}
		// エラーがあった場合に、作物登録画面を返し、エラーメッセージを表示
		if (imageErrorCount > 0) {
			// メッセージリストをモデルに設定
			model.addAttribute("notFieldMessages", messagelist);
			// 画像リストをモデルに設定
			model.addAttribute("list", imageList);
			// 新しく生成した作物フォーム（表示用）をモデルに設定
			model.addAttribute("form", newform);
			// アラートメッセージをモデルに設定
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-danger");
			model.addAttribute("message", "画像の編集に失敗しました。");
			// 作物画像編集画面を返す
			return "crops/editImage";
		}
		// エラーが無かった場合の処理
		// トップ画像、その他画像の登録
		// 一意の画像ファイル名を作成する為に、ファイル名に付ける日付文字列を生成
		String formattedDateTime = getFormattedDateTime();
		// 画像エンティティに保存するアップロードパスの生成
		Path uploadPath = Path.of(SET_PATH);
		// トップ画像が選択されている場合に行う、トップ画像の保存、エンティティの登録
		if (topImage != null && !topImage.isEmpty()) {
			// 日付文字列、画像の元のファイル名、アップロードパスから、ファイルパスを生成
			Path filePath = uploadPath.resolve(formattedDateTime + topImage.getOriginalFilename());
			// 作物エンティティからIDを取得
			Long cropId = entity.getId();
			// ファイルパスから画像が表示されるための適切なパス文字列を生成
			String path = StringUtils.cleanPath("/" + filePath.toString());
			// 作物画像エンティティを生成（トップ画像なので、topImageの値を”true”に設定）
			CropImage imageEntiy = new CropImage(cropId, path, true);
			// 作物画像エンティティを保存
			imageRepository.saveAndFlush(imageEntiy);
			// 画像の保存
			saveFile(topImage, formattedDateTime);
		}
		// その他画像が１枚以上選択されている場合に行う、その他画像の保存、エンティティの登録
		if (images != null && images.length > 0 && !images[0].isEmpty()) {
			// その他画像の配列に存在している全ての画像に対して行う処理
			for (MultipartFile image : images) {
				// 日付文字列、画像の元のファイル名、アップロードパスから、ファイルパスを生成
				Path filePath = uploadPath.resolve(formattedDateTime + image.getOriginalFilename());
				// 作物エンティティからIDを取得
				Long cropId = entity.getId();
				// ファイルパスから画像が表示されるための適切なパス文字列を生成
				String path = StringUtils.cleanPath("/" + filePath.toString());
				// 作物画像エンティティを生成（その他画像なので、topImageの値を”false”に設定）
				CropImage imageEntiy = new CropImage(cropId, path, false);
				// 作物画像エンティティを保存
				imageRepository.saveAndFlush(imageEntiy);
				// 画像の保存
				saveFile(image, formattedDateTime);
			}
		}
		// アラートメッセージをリダイレクト先のモデルに設定
		redirAttrs.addFlashAttribute("hasMessage", true);
		redirAttrs.addFlashAttribute("class", "alert-info");
		redirAttrs.addFlashAttribute("message", "画像の編集が完了しました。");
		// リダイレクト先に作物画像が紐づく作物IDのパラメータを渡し、作物詳細画面を表示
		return "redirect:/crops/edit/" + form.getId();
	}

	/**
	 * 作物画像の削除<br>
	 * 作物画像編集画面にて、登録済みの作物画像を削除する際に送られる
	 *
	 * @param imageId    削除する作物画像のID
	 * @param cropId     作物画像が紐づく作物ID
	 * @param model      ビューで使用するモデル
	 * @param redirAttrs リダイレクト属性
	 * @return 作物画像編集画面
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	@GetMapping(value = "/crops/delete-image")
	public String deleteImage(@RequestParam Long imageId, @RequestParam Long cropId, Model model,
			RedirectAttributes redirAttrs) throws IOException {
		// 作物画像IDから作物画像を削除
		imageRepository.deleteById(imageId);
		// アラートメッセージをリダイレクト先のモデルに設定
		redirAttrs.addFlashAttribute("hasMessage", true);
		redirAttrs.addFlashAttribute("class", "alert-info");
		redirAttrs.addFlashAttribute("message", "画像の削除が完了しました。");
		// リダイレクト先に削除した作物画像が紐づく作物IDのパラメータを渡し、作物画像編集画面を表示
		return "redirect:/crops/edit-image/" + cropId;
	}

	/**
	 * 作物編集画面の表示<br>
	 * 作物画像編集画面からこちらに遷移
	 *
	 * @param cropId 編集する作物のID
	 * @param model  ビューで使用するモデル
	 * @return 作物編集画面
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	@GetMapping(path = "/crops/edit/{cropId}")
	public String showEditPage(@PathVariable Long cropId, Model model) throws IOException {
		// 作物IDから作物を取得
		Optional<Crop> optionalCrop = repository.findById(cropId);
		Crop entity = optionalCrop.orElseThrow(() -> new RuntimeException("Crop not found"));
		// 作物から作物フォームを生成
		CropForm form = getCropForm(entity);
		// 作物に紐づく全ての作物画像を表示する為の画像リストを生成
		List<CropImage> imageList = imageRepository.findAllByCropIdOrderByUpdatedAtAsc(form.getId());
		// 生成した作物フォームをモデルに設定
		model.addAttribute("form", form);
		// 生成した画像リストをモデルに設定
		model.addAttribute("list", imageList);
		// 作物編集画面を返す
		return "crops/edit";
	}

	/**
	 * 作物の編集<br>
	 * 作物編集画面から送信されたフォームデータの検証、保存を行う
	 * 
	 * @param principal  認証情報
	 * @param form       作物編集フォームデータ
	 * @param result     フォーム検証の結果
	 * @param model      ビューで使用するモデル
	 * @param redirAttrs リダイレクト属性
	 * @return 検証結果に基づく画面
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	@PostMapping(path = "/crops/edit-complete")
	public String edit(Principal principal, @Validated @ModelAttribute("form") CropForm form, BindingResult result,
			Model model, RedirectAttributes redirAttrs) throws IOException {
		// 認証情報からユーザーを取得
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		// フォームデータを変数に代入
		Long userId = user.getUserId();
		Long id = form.getId();
		String name = form.getName();
		Date sowing_start = form.getSowing_start();
		Date sowing_end = form.getSowing_end();
		int cultivationp_period = form.getCultivationp_period();
		String manual = form.getManual();
		// フォームデータの作物IDから作物を取得
		Optional<Crop> optionalCrop = repository.findById(id);
		Crop entity = optionalCrop.orElseThrow(() -> new RuntimeException("Crop not found"));
		// 作物名を変更し、かつ他に同一の名前の作物が存在する場合に返すエラーの追加
		if (!entity.getName().equals(name) && repository.findByNameAndUserId(name, userId) != null) {
			FieldError fieldError = new FieldError(result.getObjectName(), "name", "その作物名は既に登録されています。");
			result.addError(fieldError);
		}
		// 作物に紐づく全ての作物画像を表示する為の画像リストを生成
		List<CropImage> imageList = imageRepository.findAllByCropIdOrderByUpdatedAtAsc(id);
		// エラーがあった場合に、作物編集画面を返し、エラーメッセージを表示
		if (result.hasErrors()) {
			// 画像リストをモデルに設定
			model.addAttribute("list", imageList);
			// アラートメッセージをモデルに設定
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-danger");
			model.addAttribute("message", "作物の編集に失敗しました。");
			// 作物編集画面を返す
			return "/crops/edit";
		}
		// エラーが無かった場合
		// フォームデータで作物エンティティを更新
		entity.setName(name);
		entity.setSowing_start(sowing_start);
		entity.setSowing_end(sowing_end);
		entity.setManual(manual);
		entity.setCultivationp_period(cultivationp_period);
		// 更新された作物エンティティを保存
		repository.saveAndFlush(entity);
		// アラートメッセージをリダイレクト先のモデルに設定
		redirAttrs.addFlashAttribute("hasMessage", true);
		redirAttrs.addFlashAttribute("class", "alert-info");
		redirAttrs.addFlashAttribute("message", "作物の編集が完了しました。");
		// リダイレクト先に編集した作物のIDのパラメータを渡し、作物詳細画面を返す
		return "redirect:/crops/detail/" + id;
	}

	/**
	 * 作物の削除
	 *
	 * @param cropId     削除する作物のID
	 * @param model      ビューで使用するモデル
	 * @param redirAttrs リダイレクト属性
	 * @return 作物一覧画面
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	@GetMapping(path = "/crops/delete/{cropId}")
	public String delete(@PathVariable Long cropId, Model model, RedirectAttributes redirAttrs) throws IOException {
		// 作物IDから作物を削除
		repository.deleteById(cropId);
		// アラートメッセージをリダイレクト先のモデルに設定
		redirAttrs.addFlashAttribute("hasMessage", true);
		redirAttrs.addFlashAttribute("class", "alert-info");
		redirAttrs.addFlashAttribute("message", "作物の削除が完了しました。");
		// 作物一覧画面を返す
		return "redirect:/crops/list";
	}

	/**
	 * 作物一覧の表示
	 *
	 * @param principal 認証情報
	 * @param model     ビューで使用するモデル
	 * @return 作物一覧画面
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	@GetMapping(path = "/crops/list")
	public String showList(Principal principal, Model model) throws IOException {
		// 認証情報からユーザ情報を取得
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		// ユーザに対応する全ての作物を取得し、リストを生成
		List<Crop> list = repository.findAllByUserIdOrderByUpdatedAtAsc(user.getUserId());
		// 生成したリストをモデルに設定
		model.addAttribute("list", list);
		// 作物一覧画面を返す
		return "/crops/list";
	}

	/**
	 * 作物の検索<br>
	 * 作物一覧画面から作物検索を行う（キーワードでの絞り込み検索機能あり）
	 *
	 * @param principal 認証情報
	 * @param keyword   検索キーワード
	 * @param model     ビューで使用するモデル
	 * @return 作物一覧画面
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	@PostMapping(path = "/crops/search")
	public String searchCrops(Principal principal, @RequestParam(name = "keyword", required = false) String keyword,
			Model model) throws IOException {
		// 認証情報からユーザ情報を取得
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		// 検索結果として返す作物リストを初期化
		List<Crop> list = new ArrayList<>();
		// キーワードが指定されていない場合はユーザに紐づく全ての作物を取得し、作物リストに代入
		if (keyword == null) {
			list = repository.findAllByUserIdOrderByUpdatedAtAsc(user.getUserId());
		}
		// キーワードが指定されている場合は、作物名にキーワードを含む作物を取得し、作物リストに代入
		else if (keyword != null) {
			list = repository.findAllByNameContainingAndUserId(keyword, user.getUserId());
		}
		// 検索結果として返す作物リストが空の場合、モデルにメッセージを設定
		if (list.isEmpty()) {
			model.addAttribute("sarchMessage", "その作物は見つかりませんでした。");
		}
		// 検索結果として返す作物リストを、モデルに設定
		model.addAttribute("list", list);
		// パラメータとして送られてきた検索キーワードをモデルに設定
		model.addAttribute("keyword", keyword);
		// 作物一覧画面を返す
		return "/crops/list";
	}

	/**
	 * Date型を指定した年のLocalDate型に変換するメソッド
	 *
	 * @param date 変換対象のDateオブジェクト
	 * @param year 指定の年
	 * @return LocalDateオブジェクト
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	// Date型をLocalDate型に変換
	public LocalDate getLocalDate(Date date, int year) throws IOException {
		// DateをLocalDate型に変換
		LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		// 年を変更
		localDate = localDate.withYear(year);
		return localDate;
	}

	/**
	 * 作物エンティティから作物フォームオブジェクトを取得するメソッド
	 *
	 * @param crop 作物エンティティ
	 * @return 作物フォームオブジェクト
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	public CropForm getCropForm(Crop crop) throws IOException {
		// ModelMapperの設定: 重複があっても無視する
		modelMapper.getConfiguration().setAmbiguityIgnored(true);
		// 作物クラスから作物フォームクラスへのマッピングの設定
		modelMapper.typeMap(Crop.class, CropForm.class);
		// 作物エンティティから作物フォームオブジェクトへの変換
		CropForm form = modelMapper.map(crop, CropForm.class);
		return form;
	}

	/**
	 * 画像の拡張子が一般的な画像形式かどうかを検証するメソッド
	 * 
	 * @param file 判定対象のMultipartFile
	 * @return 画像ファイルの場合はtrue、それ以外の場合はfalse
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	private boolean isImageFile(MultipartFile file) throws IOException {
		// 画像ファイルの元の名前を取得
		String fileName = file.getOriginalFilename();
		// 元の画像ファイル名がnuｌｌでなく、画像の拡張子が画像形式であれば、trueを返し、そうでなければfalseを返す
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
