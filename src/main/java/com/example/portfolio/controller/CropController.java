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
import java.util.List;
import java.util.Locale;
import java.util.Optional;

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
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;

import com.example.portfolio.repository.CropRepository;
import com.example.portfolio.repository.CropImageReposiory;
import com.example.portfolio.entity.Crop;
import com.example.portfolio.entity.CropImage;
import com.example.portfolio.entity.UserInf;
import com.example.portfolio.form.CropForm;
import com.example.portfolio.form.CropImageForm;
import com.example.portfolio.form.UserForm;

import org.springframework.beans.BeanUtils;

@Controller
//@SessionAttributes("cropform") // 必要なければ削除
public class CropController {

	protected static Logger log = LoggerFactory.getLogger(CropController.class);

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	CropRepository repository;

	@Autowired
	CropImageReposiory imageRepository;

//	// 必要なければ削除
//	@ModelAttribute("cropform")
//	public CropForm getCropForm() {
//		return new CropForm();
//	}

	private static final String UPLOAD_DIR = "uploads";

	// 月ごとの作物表示
	@GetMapping(path = "/crops")
	public String index(@ModelAttribute("month") String month, Principal principal, Model model) throws IOException {
		// User情報の取得
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();

		// 該当月に種まき時期が該当する作物リストを作成
		Iterable<Crop> monthCrops = new ArrayList<Crop>();
		// 現在の年を取得
		int year = LocalDate.now().getYear();
		// 年と月から、年月を取得
		YearMonth yearMonth = YearMonth.of(year, Month.valueOf(month));

		// その月の最初の日付を取得
		LocalDate firstDayOfMonth = yearMonth.atDay(1);

		// その月の最後の日付を取得
		LocalDate lastDayOfMonth = yearMonth.atEndOfMonth();

		// 該当のユーザーのすべての作物のリストを作成
		Iterable<Crop> allCrops = repository.findAllByUserIdOrderByUpdatedAtDesc(user.getUserId());
		// 作物リストallCropsから、一つずつ作物取り出し、該当月に種まき時期が該当するか否かの判定
		for (Crop crop : allCrops) {
			// sowing_startをDate型からLoccalDate型に変換
			LocalDate sowing_start_localDate = crop.getSowing_start().atYear(year);
			// sowing_startをDate型からLoccalDate型に変換
			LocalDate sowing_end_localDate = crop.getSowing_end().atYear(year);
			// 判定を行い、判定を通過した作物を作物リストmonthCropsに追加
			if ((!sowing_start_localDate.isBefore(firstDayOfMonth) && !sowing_start_localDate.isAfter(lastDayOfMonth))
					|| (!sowing_end_localDate.isBefore(firstDayOfMonth)
							&& !sowing_end_localDate.isAfter(lastDayOfMonth))) {
				((ArrayList<Crop>) monthCrops).add(crop);
			}
		}
		model.addAttribute("list", monthCrops);
		model.addAttribute("month", Month.valueOf(month).getDisplayName(TextStyle.FULL, Locale.JAPANESE) );
		return "crops/index";
	}

	// 作物データ新規登録formの表示
	@GetMapping(path = "/crops/new")
	public String newTopic(Model model) {
		CropForm cropform = new CropForm();
		model.addAttribute("cropform", cropform);
		return "crops/new";

	}

	// formから送られた作物データを登録
	@RequestMapping(value = "/crop", method = RequestMethod.POST)
	public String create(Principal principal, @Validated @ModelAttribute("cropform") CropForm form,
			BindingResult result, Model model, RedirectAttributes attributes)
			throws IOException {

		// 同一の作物が既に登録されている場合、エラー項目に追加
		if (repository.findByName(form.getName()) != null) {
			FieldError fieldError = new FieldError(result.getObjectName(), "name", "その作物はすでに登録されています。");
			result.addError(fieldError);
		}
		// エラーがある場合、エラー文を表示しformを再度返す

		if (result.hasErrors()) {
			model.addAttribute("form", form);
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-danger");
			model.addAttribute("message", "作物データ登録に失敗しました。");
			return "crops/new";
		}

//		//CropEntityのインスタンスを生成
		Crop entity = new Crop();
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		entity.setUserId(user.getUserId());
		entity.setName(form.getName());
		entity.setManual(form.getManual());
		entity.setSowing_start(form.getSowing_start());
		entity.setSowing_end(form.getSowing_end());
		entity.setHarvest_start(form.getHarvest_start());
		entity.setHarvest_end(form.getHarvest_end());
		entity.setCultivationp_period(form.getCultivationp_period());

		// entityの保存
		repository.saveAndFlush(entity);
		// 画像を表示するたmodelめのimageListを作成
		attributes.addFlashAttribute("hasMessage", true);
		attributes.addFlashAttribute("class", "alert-info");
		attributes.addFlashAttribute("message", "作物データ登録に成功しました。");
		attributes.addFlashAttribute("cropId", entity.getId());
		return "redirect:/crops/newImage/" + entity.getId();

	}

	@GetMapping(path = "/crops/newImage/{cropId}")
	public String newImage(@PathVariable Long cropId, Model model) {
		CropImageForm cropImageform = new CropImageForm();
		cropImageform.setCropId(cropId);
		CropImageForm cropTopImageform = new CropImageForm();
		cropTopImageform.setCropId(cropId);
		Optional<Crop> optionalCrop = repository.findById(cropId);
		Crop crop = optionalCrop.orElseThrow(() -> new RuntimeException("Crop not found")); // もし
																							// Optionalが空の場合は例外をスローするなどの対処
		model.addAttribute("crop", crop);
		// List<CropImage> topImage =
		// imageRepository.findByCropIdAndTopImageTrue(cropId);
		Iterable<CropImage> imageList = imageRepository.findAllByCropIdAndTopImageFalseOrderByUpdatedAtDesc(cropId);
		// model.addAttribute("list", topImage);
		model.addAttribute("list", imageList);

		model.addAttribute("cropImageform", cropImageform);
		model.addAttribute("cropTopImageform", cropTopImageform);
		return "crops/newImage";

	}

	// トップ画像の保存
	@RequestMapping(value = "/crops/upload-TopImage", method = RequestMethod.POST)
	public String uploadTopImage(Principal principal,
			@Validated @ModelAttribute("cropTopImageform") CropImageForm imageform, BindingResult result, Model model,
			@RequestParam MultipartFile image, RedirectAttributes attributes) throws IOException {
		// トップ画像の登録が既にないかの検証をエラー項目に追加
		if (image != null && imageRepository.findByCropIdAndTopImageTrue(imageform.getCropId()).size() > 0) {
			attributes.addFlashAttribute("imageError", "トップ画像のアップロードは一枚までです。");
			FieldError fieldError = new FieldError(result.getObjectName(), "image", "トップ画像のアップロードは一枚までです。");
						result.addError(fieldError);
		}
		// ファイルの拡張子が画像形式かどうかの検証をエラー項目に追加
		if (image != null && !isImageFile(image)) {
			attributes.addFlashAttribute("imageError", "画像ファイル形式が正しくありません。");
			FieldError fieldError = new FieldError(result.getObjectName(), "image", "画像ファイル形式が正しくありません。");
			result.addError(fieldError);
		}

		// エラーがある場合、エラー文を表示し、新しいformを送信

		if (result.hasErrors()) {
			attributes.addFlashAttribute("hasMessage", true);
			attributes.addFlashAttribute("class", "alert-danger");
			attributes.addFlashAttribute("message", "画像のアップロードに失敗しました。");
			return "redirect:/crops/newImage/" + imageform.getCropId();
		}

		// CropImagEntityのインスタンスを生成
		CropImage cropImage = new CropImage();
		Path uploadPath = Path.of("images", UPLOAD_DIR);
		Path filePath = uploadPath.resolve(image.getOriginalFilename());
		cropImage.setCropId(imageform.getCropId());
		cropImage.setPath("/" + filePath.toString().replace("\\", "/"));
		cropImage.setTopImage(true);
		saveFile(image);
		// entityの保存
		imageRepository.saveAndFlush(cropImage);
		attributes.addFlashAttribute("hasMessage", true);
		attributes.addFlashAttribute("class", "alert-info");
		attributes.addFlashAttribute("message", "画像のアップロードに成功しました。");
		return "redirect:/crops/newImage/" + imageform.getCropId();
		

	}

	// 画像の保存
	@RequestMapping(value = "/crops/upload-image", method = RequestMethod.POST)
	public String uploadImage(Principal principal, @Validated @ModelAttribute("cropImageform") CropImageForm imageform,
			BindingResult result, Model model, @RequestParam MultipartFile image, RedirectAttributes attributes)
			throws IOException {
		// ファイルの拡張子が画像形式かどうかの検証をエラー項目に追加
		if (image != null && !isImageFile(image)) {
			attributes.addFlashAttribute("imageError", "画像ファイル形式が正しくありません。");
		FieldError fieldError = new FieldError(result.getObjectName(), "image", "画像ファイル形式が正しくありません。");
					result.addError(fieldError);
		}
		// エラーがある場合、エラー文を表示し、新しいformを送信

		if (result.hasErrors()) {
			attributes.addFlashAttribute("hasMessage", true);
			attributes.addFlashAttribute("class", "alert-danger");
			attributes.addFlashAttribute("message", "画像のアップロードに失敗しました。");
			return "redirect:/crops/newImage/" + imageform.getCropId();
		}

		// CropImagEntityのインスタンスを生成
		CropImage cropImage = new CropImage();
		Path uploadPath = Path.of("images", UPLOAD_DIR);
		Path filePath = uploadPath.resolve(image.getOriginalFilename());
		cropImage.setCropId(imageform.getCropId());
		cropImage.setPath("/" + filePath.toString().replace("\\", "/"));
		cropImage.setTopImage(false);
		saveFile(image);
		// entityの保存
		imageRepository.saveAndFlush(cropImage);
		attributes.addFlashAttribute("hasMessage", true);
		attributes.addFlashAttribute("class", "alert-info");
		attributes.addFlashAttribute("message", "画像のアップロードに成功しました。");
		return "redirect:/crops/newImage/" + imageform.getCropId();
	}

	private boolean isImageFile(MultipartFile file) {
		// 拡張子が一般的な画像形式かどうかの検証。
		String fileName = file.getOriginalFilename();
		return fileName != null
				&& (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png"));
	}

	private void saveFile(MultipartFile image) throws IOException {
		Path uploadPath = Path.of("src", "main", "resources", "static", "images", UPLOAD_DIR);

		// ディレクトリが存在しない場合は作成
		if (!Files.exists(uploadPath)) {
			Files.createDirectories(uploadPath);
		}

		// ファイルを保存
		try (var inputStream = image.getInputStream()) {
			Path filePath = uploadPath.resolve(image.getOriginalFilename());
			Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
		}
	}

	// 作物の削除
	@GetMapping(value = "/crops/delete-image")
	public String deleteImage(@RequestParam Long imageId, @RequestParam Long cropId, Model model,
			RedirectAttributes attributes) throws IOException {
		Optional<CropImage> entity = imageRepository.findById(imageId);
		// 画面が保持されるか要確認
		if (entity == null) {
			attributes.addFlashAttribute("hasMessage", true);
			attributes.addFlashAttribute("class", "alert-danger");
			attributes.addFlashAttribute("message", "その画像は存在しません。");
			return "redirect:/crops/newImage/" + cropId;
		}
		imageRepository.deleteById(imageId);
		attributes.addFlashAttribute("hasMessage", true);
		attributes.addFlashAttribute("class", "alert-info");
		attributes.addFlashAttribute("message", "画像の削除に成功しました。");
		return "redirect:/crops/newImage/" + cropId;
	}

	@GetMapping(path = "/crops/detail/{cropId}")
	public String showDetail(@PathVariable Long cropId, Model model) throws IOException {
		Optional<Crop> optionalCrop = repository.findById(cropId);
		Crop crop = optionalCrop.orElseThrow(() -> new RuntimeException("Crop not found")); // もし Optional //
																							// が空の場合は例外をスローするなどの対処
		model.addAttribute("crop", crop);
		Iterable<CropImage> imageList = imageRepository.findAllByCropIdAndTopImageFalseOrderByUpdatedAtDesc(cropId);
		model.addAttribute("list", imageList);
		return "crops/detail";
	}

	// 編集画面
	@GetMapping(path = "/crops/edit/{cropId}")
	public String showEditPage(@PathVariable Long cropId, Model model) throws IOException {
		Optional<Crop> optionalCrop = repository.findById(cropId);
		Crop crop = optionalCrop.orElseThrow(() -> new RuntimeException("Crop not found")); // もし Optional
		CropForm form = getCrop(crop);
		model.addAttribute("form", form);
		return "crops/edit";
	}

	public CropForm getCrop(Crop crop) throws IOException {
		modelMapper.getConfiguration().setAmbiguityIgnored(true);
		modelMapper.typeMap(Crop.class, CropForm.class);
		CropForm form = modelMapper.map(crop, CropForm.class);
		return form;
	}

	// 編集データの送信
	@RequestMapping(value = "/crops/edit-complete", method = RequestMethod.POST)
	public String edit(@Validated @ModelAttribute("form") CropForm form, BindingResult result, Model model,
			RedirectAttributes attributes) throws IOException {
		Optional<Crop> optionalCrop = repository.findById(form.getId());
		Crop crop = optionalCrop.orElseThrow(() -> new RuntimeException("Crop not found")); // もし Optional

		// 名前を変更し、他に同一の名前の作物が存在する場合、エラーを返す
		if (!crop.getName().equals(form.getName()) && repository.findByName(form.getName()) != null) {
			FieldError fieldError = new FieldError(result.getObjectName(), "name", "その作物名は使用できません。");
			result.addError(fieldError);
		}
		// エラーがある場合、エラー文を表示し、新しいformを送信
		if (result.hasErrors()) {
			model.addAttribute("form", form);
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-danger");
			model.addAttribute("message", "作物データ編集に失敗しました。");
			// データを保持する処理
			return "/crops/edit";
		}

		// CropEntityのフィールド値を更新
		crop.setName(form.getName());
		crop.setSowing_start(form.getSowing_start());
		crop.setSowing_end(form.getSowing_end());
		crop.setHarvest_start(form.getHarvest_start());
		crop.setHarvest_end(form.getHarvest_end());
		crop.setManual(form.getManual());
		crop.setCultivationp_period(form.getCultivationp_period());
		// entityの保存
		repository.saveAndFlush(crop);
		// 画像を表示するためのimageListを作成
		Iterable<CropImage> imageList = imageRepository
				.findAllByCropIdAndTopImageFalseOrderByUpdatedAtDesc(form.getId());
		model.addAttribute("list", imageList);

		attributes.addFlashAttribute("hasMessage", true);
		attributes.addFlashAttribute("class", "alert-info");
		attributes.addFlashAttribute("message", "作物情報登録に成功しました。");
		attributes.addFlashAttribute("cropId", form.getId());
		return "redirect:/crops/editImage/" + form.getId();

	}

	@GetMapping(path = "/crops/editImage/{cropId}")
	public String editImage(@PathVariable Long cropId, Model model) {
		CropImageForm cropImageform = new CropImageForm();
		cropImageform.setCropId(cropId);
		CropImageForm cropTopImageform = new CropImageForm();
		cropTopImageform.setCropId(cropId);
		Optional<Crop> optionalCrop = repository.findById(cropId);
		Crop crop = optionalCrop.orElseThrow(() -> new RuntimeException("Crop not found")); // もし
																							// Optionalが空の場合は例外をスローするなどの対処
		model.addAttribute("crop", crop);
		Iterable<CropImage> imageList = imageRepository.findAllByCropIdAndTopImageFalseOrderByUpdatedAtDesc(cropId);
		model.addAttribute("list", imageList);
		model.addAttribute("cropImageform", cropImageform);
		model.addAttribute("cropTopImageform", cropTopImageform);
		return "crops/newImage";

	}

	@RequestMapping(value = "/crops/delete/{cropId}", method = RequestMethod.POST)
	public String delete(@PathVariable Long cropId, BindingResult result, RedirectAttributes redirAttrs, Model model)
			throws IOException {
		repository.deleteById(cropId);
		Iterable<CropImage> imageList = imageRepository.findAllByCropIdOrderByUpdatedAtDesc(cropId);
		imageRepository.deleteAll(imageList);
		redirAttrs.addFlashAttribute("hasMessage", true);
		redirAttrs.addFlashAttribute("class", "alert-info");
		redirAttrs.addFlashAttribute("message", "作物データの削除に成功しました。");
		return "/crops/list";
	}

	// 作物一覧画面表示
	@GetMapping(path = "/crops/list")
	public String showList(Principal principal, Model model) throws IOException {
		// User情報の取得
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		// 該当のユーザーのすべての作物のリストを作成
		Iterable<Crop> list = repository.findAllByUserIdOrderByUpdatedAtDesc(user.getUserId());
		model.addAttribute("list", list);
		return "/crops/list";
	}

	// 作物検索機能
	@RequestMapping(value = "/crops/search", method = RequestMethod.POST)
	public String searchCrops(@RequestParam("keyword") String keyword, Model model, RedirectAttributes redirAttrs) {
		List<Crop> searchResults = repository.findByNameContaining(keyword);
		if (searchResults.isEmpty()) {
			redirAttrs.addFlashAttribute("message", "その作物は見つかりませんでした。");
			return "redirect:/crops/list";
		}
		redirAttrs.addFlashAttribute("searchResults", searchResults);
		return "redirect:/crops/list";

	}

}
