package com.example.portfolio.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.MonthDay;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import com.example.portfolio.repository.CropRepository;
import com.example.portfolio.repository.CropImageReposiory;
import com.example.portfolio.entity.Crop;
import com.example.portfolio.entity.CropImage;
import com.example.portfolio.entity.UserInf;
import com.example.portfolio.form.CropForm;

@Controller
public class CropController {

	protected static Logger log = LoggerFactory.getLogger(CropController.class);

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	CropRepository repository;

	@Autowired
	CropImageReposiory imageRepository;

	private static final String UPLOAD_DIR = "uploads";

	// 月ごとの作物表示
	@GetMapping(path = "/crops")
	public String index(Principal principal, @ModelAttribute("month") String month, Model model) throws IOException {
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		List<Crop> monthCrops = new ArrayList<Crop>();
		int year = LocalDate.now().getYear();
		YearMonth yearMonth = YearMonth.of(year, Month.valueOf(month));
		// 該当月の最初の日付を取得
		LocalDate firstDayOfMonth = yearMonth.atDay(1);
		// 該当月の最後の日付を取得
		LocalDate lastDayOfMonth = yearMonth.atEndOfMonth();
		List<Crop> allCrops = repository.findAllByUserIdOrderByUpdatedAtAsc(user.getUserId());
		// 作物が該当月に種まき時期が含まれていれば、monthCropsに追加
		for (Crop crop : allCrops) {
			LocalDate sowing_start_localDate = crop.getSowing_start().atYear(year);
			LocalDate sowing_end_localDate = crop.getSowing_end().atYear(year);
			// 栽培期間が年を跨ぐ場合の処理
			if (sowing_end_localDate.isBefore(sowing_start_localDate)) {
				int nextYear = LocalDate.now().getYear() + 1;
				sowing_end_localDate = crop.getSowing_end().atYear(nextYear);
				int sowing_end_month = sowing_end_localDate.getMonthValue();
				for (int i = 1; i <= sowing_end_month; i++) {
					if (Month.valueOf(month).getValue() == i) {
						yearMonth = YearMonth.of(nextYear, Month.valueOf(month));
						firstDayOfMonth = yearMonth.atDay(1);
						lastDayOfMonth = yearMonth.atEndOfMonth();
					}
				}
			}
			if (!(sowing_end_localDate.isBefore(firstDayOfMonth) || sowing_start_localDate.isAfter(lastDayOfMonth))) {
				monthCrops.add(crop);
			}
		}

		// 月名を日本語に変換
		String JapaneseMonth = Month.valueOf(month).getDisplayName(TextStyle.FULL, Locale.JAPANESE);
		model.addAttribute("list", monthCrops);
		model.addAttribute("month", JapaneseMonth);
		return "crops/index";
	}

	// 作物登録画面表示
	@GetMapping(path = "/crops/new")
	public String newTopic(Model model) {
		CropForm form = new CropForm();
		model.addAttribute("form", form);
		return "crops/new";
	}

	// 作物登録
	@PostMapping(path = "/crop")
	public String create(Principal principal, @RequestParam("topImage") MultipartFile topImage,
			@RequestParam("images") MultipartFile[] images, @Validated @ModelAttribute("form") CropForm form,
			BindingResult result, Model model, RedirectAttributes redirAttrs) throws IOException {
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		Long userId = user.getUserId();
		String name = form.getName();
		MonthDay sowing_start = form.getSowing_start();
		MonthDay sowing_end = form.getSowing_end();
		int cultivationp_period = form.getCultivationp_period();
		String manual = form.getManual();
		// formのフィールドエラー以外のエラー（今回は画像のエラー）リストのカウンター
		int imageErrorCount = 0;
		// formのフィールドエラー以外のエラーリストを生成
		List<String> messagelist = new ArrayList<>();
		// トップ画像ファイルの拡張子が画像形式でない場合に返すエラーの追加
		if (topImage != null && !topImage.isEmpty() && !isImageFile(topImage)) {
			model.addAttribute("topImageError", "トップ画像のファイル形式が正しくありません。");
			messagelist.add("トップ画像のファイル形式が正しくありません。");
			imageErrorCount++;
		}
		// その他画像ファイルの拡張子が画像形式でない場合に返すエラーの追加
		if (images != null && images.length > 0 && !images[0].isEmpty()) {
			for (MultipartFile image : images) {
				if (image != null && !isImageFile(image)) {
					model.addAttribute("imageError", "その他画像のファイル形式が正しくありません。");
					messagelist.add("その他画像のファイル形式が正しくありません。");
					imageErrorCount++;
				}
			}
		}
		// 同一の作物が既に登録されている場合に返すエラーの追加
		if (repository.findByNameAndUserId(name, userId) != null) {
			FieldError fieldError = new FieldError(result.getObjectName(), "cropName", "その作物は既に登録されています。");
			result.addError(fieldError);
		}
		if (imageErrorCount > 0 || result.hasErrors()) {
			model.addAttribute("notFieldMessages", messagelist);
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-danger");
			model.addAttribute("message", "作物の登録に失敗しました。");
			return "crops/new";
		}
		Crop entity = new Crop(userId, name, sowing_start, sowing_end, cultivationp_period, manual);
		repository.saveAndFlush(entity);
		String formattedDateTime = getFormattedDateTime();
		Path uploadPath = Path.of("images", UPLOAD_DIR);
		// トップ画像の保存、エンティティ登録
		if (topImage != null && !topImage.isEmpty()) {
			Path filePath = uploadPath.resolve(formattedDateTime + topImage.getOriginalFilename());
			Long cropId = entity.getId();
			String path = "/" + filePath.toString().replace("\\", "/");
			CropImage imageEntiy = new CropImage(cropId, path, true);
			saveFile(topImage, formattedDateTime);
			imageRepository.saveAndFlush(imageEntiy);
		}
		// その他画像の保存、エンティティ登録
		if (images != null && images.length > 0 && !images[0].isEmpty()) {
			for (MultipartFile image : images) {
				Path filePath = uploadPath.resolve(formattedDateTime + image.getOriginalFilename());
				Long cropId = entity.getId();
				String path = "/" + filePath.toString().replace("\\", "/");
				CropImage imageEntiy = new CropImage(cropId, path, false);
				saveFile(image, formattedDateTime);
				imageRepository.saveAndFlush(imageEntiy);
			}
		}
		redirAttrs.addFlashAttribute("hasMessage", true);
		redirAttrs.addFlashAttribute("class", "alert-info");
		redirAttrs.addFlashAttribute("message", "作物の登録が完了しました。");
		return "redirect:/crops/detail/" + entity.getId();
	}

	// 作物詳細表示
	@GetMapping(path = "/crops/detail/{cropId}")
	public String showDetail(@PathVariable Long cropId, Model model) throws IOException {
		Optional<Crop> optionalCrop = repository.findById(cropId);
		Crop entity = optionalCrop.orElseThrow(() -> new RuntimeException("Crop not found"));
		List<CropImage> imageList = imageRepository.findAllByCropIdOrderByUpdatedAtAsc(cropId);
		model.addAttribute("crop", entity);
		model.addAttribute("list", imageList);
		return "crops/detail";
	}

	// 作物画像編集画面表示
	@GetMapping(path = "/crops/edit-image/{cropId}")
	public String showImageEditPage(@PathVariable Long cropId, Model model) throws IOException {
		Optional<Crop> optionalCrop = repository.findById(cropId);
		Crop entity = optionalCrop.orElseThrow(() -> new RuntimeException("Crop not found"));
		CropForm form = getCropForm(entity);
		List<CropImage> imageList = imageRepository.findAllByCropIdOrderByUpdatedAtAsc(cropId);
		model.addAttribute("form", form);
		model.addAttribute("list", imageList);
		return "crops/editImage";
	}

	// 作物画像編集
	@PostMapping(path = "/crops/edit-image-complete")
	public String editImage(Principal principal, @RequestParam("topImage") MultipartFile topImage,
			@RequestParam("images") MultipartFile[] images, @ModelAttribute("form") CropForm form, BindingResult result,
			Model model) throws IOException {
		Optional<Crop> optionalCrop = repository.findById(form.getId());
		Crop entity = optionalCrop.orElseThrow(() -> new RuntimeException("Crop not found"));
		CropForm newform = getCropForm(entity);
		List<CropImage> imageList = imageRepository.findAllByCropIdOrderByUpdatedAtAsc(form.getId());
		int imageErrorCount = 0;
		List<String> messagelist = new ArrayList<>();
		// トップ画像の登録が既にある場合に返すエラーの追加
		if (topImage != null && !topImage.isEmpty()
				&& imageRepository.findByCropIdAndTopImageTrue(form.getId()).size() > 0) {
			model.addAttribute("topImageError", "トップ画像の登録は1itemまでです。");
			messagelist.add("トップ画像の登録は1itemまでです。");
			imageErrorCount++;
		}
		// トップ画像ファイルの拡張子が画像形式でない場合に返すエラーの追加
		else if (topImage != null && !topImage.isEmpty() && !isImageFile(topImage)) {
			model.addAttribute("topImageError", "トップ画像のファイル形式が正しくありません。");
			messagelist.add("トップ画像のファイル形式が正しくありません。");
			imageErrorCount++;
		}
		// その他画像ファイルの拡張子が画像形式でない場合に返すエラーの追加
		if (images != null && images.length > 0 && !images[0].isEmpty()) {
			for (MultipartFile image : images) {
				if (image != null && !isImageFile(image)) {
					model.addAttribute("imageError", "その他画像のファイル形式が正しくありません。");
					messagelist.add("その他画像のファイル形式が正しくありません。");
					imageErrorCount++;
				}
			}
		}
		if (imageErrorCount > 0 || result.hasErrors()) {
			model.addAttribute("notFieldMessages", messagelist);
			model.addAttribute("list", imageList);
			model.addAttribute("form", newform);
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-danger");
			model.addAttribute("message", "画像の編集に失敗しました。");
			return "crops/editImage";
		}
		String formattedDateTime = getFormattedDateTime();
		Path uploadPath = Path.of("images", UPLOAD_DIR);
		// トップ画像の保存、エンティティ登録
		if (topImage != null && !topImage.isEmpty()) {
			Path filePath = uploadPath.resolve(formattedDateTime + topImage.getOriginalFilename());
			Long cropId = form.getId();
			String path = "/" + filePath.toString().replace("\\", "/");
			CropImage imageEntiy = new CropImage(cropId, path, true);
			saveFile(topImage, formattedDateTime);
			imageRepository.saveAndFlush(imageEntiy);
		}
		// その他画像の保存、エンティティ登録
		if (images != null && images.length > 0 && !images[0].isEmpty()) {
			for (MultipartFile image : images) {
				Path filePath = uploadPath.resolve(formattedDateTime + image.getOriginalFilename());
				Long cropId = form.getId();
				String path = "/" + filePath.toString().replace("\\", "/");
				CropImage imageEntiy = new CropImage(cropId, path, false);
				saveFile(image, formattedDateTime);
				imageRepository.saveAndFlush(imageEntiy);
			}
		}
		model.addAttribute("hasMessage", true);
		model.addAttribute("class", "alert-info");
		model.addAttribute("message", "画像の編集が完了しました。");
		return "redirect:/crops/edit/" + form.getId();
	}

	// 作物画像削除
	@GetMapping(value = "/crops/delete-image")
	public String deleteImage(@RequestParam Long imageId, @RequestParam Long cropId, Model model,
			RedirectAttributes attributes) throws IOException {
		imageRepository.deleteById(imageId);
		attributes.addFlashAttribute("hasMessage", true);
		attributes.addFlashAttribute("class", "alert-info");
		attributes.addFlashAttribute("message", "画像の削除が完了しました。");
		return "redirect:/crops/edit-image/" + cropId;
	}

	// 作物編集画面表示
	@GetMapping(path = "/crops/edit/{cropId}")
	public String showEditPage(@PathVariable Long cropId, Model model) throws IOException {
		Optional<Crop> optionalCrop = repository.findById(cropId);
		Crop entity = optionalCrop.orElseThrow(() -> new RuntimeException("Crop not found"));
		CropForm form = getCropForm(entity);
		List<CropImage> imageList = imageRepository.findAllByCropIdOrderByUpdatedAtAsc(form.getId());
		model.addAttribute("form", form);
		model.addAttribute("list", imageList);
		return "crops/edit";
	}

	// 作物編集
	@PostMapping(path = "/crops/edit-complete")
	public String edit(Principal principal, @Validated @ModelAttribute("form") CropForm form, BindingResult result,
			Model model, RedirectAttributes attributes) throws IOException {
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		Long userId = user.getUserId();
		Long id = form.getId();
		String name = form.getName();
		MonthDay sowing_start = form.getSowing_start();
		MonthDay sowing_end = form.getSowing_end();
		int cultivationp_period = form.getCultivationp_period();
		String manual = form.getManual();
		Optional<Crop> optionalCrop = repository.findById(id);
		Crop entity = optionalCrop.orElseThrow(() -> new RuntimeException("Crop not found"));
		// 作物名を変更し、他に同一の名前の作物が存在する場合に返すエラーの追加
		if (!entity.getName().equals(name) && repository.findByNameAndUserId(name, userId) != null) {
			FieldError fieldError = new FieldError(result.getObjectName(), "name", "その作物名は既に登録されています。");
			result.addError(fieldError);
		}
		List<CropImage> imageList = imageRepository.findAllByCropIdOrderByUpdatedAtAsc(id);
		if (result.hasErrors()) {
			model.addAttribute("list", imageList);
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-danger");
			model.addAttribute("message", "作物の編集に失敗しました。");
			return "/crops/edit";
		}
		entity.setName(name);
		entity.setSowing_start(sowing_start);
		entity.setSowing_end(sowing_end);
		entity.setManual(manual);
		entity.setCultivationp_period(cultivationp_period);
		repository.saveAndFlush(entity);
		attributes.addFlashAttribute("hasMessage", true);
		attributes.addFlashAttribute("class", "alert-info");
		attributes.addFlashAttribute("message", "作物の編集が完了しました。");
		return "redirect:/crops/detail/" + id;
	}

	// 作物削除
	@GetMapping(path = "/crops/delete/{cropId}")
	public String delete(@PathVariable Long cropId, Model model, RedirectAttributes redirAttrs) throws IOException {
		repository.deleteById(cropId);
		redirAttrs.addFlashAttribute("hasMessage", true);
		redirAttrs.addFlashAttribute("class", "alert-info");
		redirAttrs.addFlashAttribute("message", "作物の削除が完了しました。");
		return "redirect:/crops/list";
	}

	// 作物一覧表示
	@GetMapping(path = "/crops/list")
	public String showList(Principal principal, Model model) throws IOException {
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		List<Crop> list = repository.findAllByUserIdOrderByUpdatedAtAsc(user.getUserId());
		model.addAttribute("list", list);
		return "/crops/list";
	}

	// 作物検索
	@PostMapping(path = "/crops/search")
	public String searchCrops(Principal principal, @RequestParam(name = "keyword", required = false) String keyword,
			Model model) {
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		List<Crop> list = new ArrayList<>();
		if (keyword == null) {
			list = repository.findAllByUserIdOrderByUpdatedAtAsc(user.getUserId());
		} else if (keyword != null) {
			list = repository.findAllByNameContainingAndUserId(keyword, user.getUserId());
		}
		if (list.isEmpty()) {
			model.addAttribute("sarchMessage", "その作物は見つかりませんでした。");
		}
		model.addAttribute("list", list);
		model.addAttribute("keyword", keyword);
		return "/crops/list";

	}

	// CropエンティティからCropFormエンティティの取得
	public CropForm getCropForm(Crop crop) throws IOException {
		modelMapper.getConfiguration().setAmbiguityIgnored(true);
		modelMapper.typeMap(Crop.class, CropForm.class);
		CropForm form = modelMapper.map(crop, CropForm.class);
		return form;
	}

	// 拡張子が一般的な画像形式かどうかの検証
	private boolean isImageFile(MultipartFile file) throws IOException {
		String fileName = file.getOriginalFilename();
		return fileName != null
				&& (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png"));
	}

	// 一意の画像ファイル名を作成する為に、ファイル名に付ける日付文字列を生成
	private String getFormattedDateTime() throws IOException {
		LocalDateTime currentDateTime = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
		String formattedDateTime = currentDateTime.format(formatter);
		return formattedDateTime;
	}

	// 画像の保存
	private void saveFile(MultipartFile image, String formattedDateTime) throws IOException {
		Path uploadPath = Path.of("src", "main", "resources", "static", "images", UPLOAD_DIR);
		// ディレクトリが存在しない場合は作成
		if (!Files.exists(uploadPath)) {
			Files.createDirectories(uploadPath);
		}
		// ファイルを保存
		try (var inputStream = image.getInputStream()) {
			Path filePath = uploadPath.resolve(formattedDateTime + image.getOriginalFilename());
			Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
		}
	}
}
