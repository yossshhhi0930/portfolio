package com.example.portfolio.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
import com.example.portfolio.form.CropImageForm;
import org.springframework.beans.BeanUtils;

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
		return "crops/index";
	}

	// 作物データ新規登録formの表示
	@GetMapping(path = "/crops/new")
	public String newTopic(Model model) {
		CropForm cropform = new CropForm();
		CropImageForm cropImageform = new CropImageForm();
		cropImageform.setCropId(cropform.getId());
		model.addAttribute("cropform", cropform );
		model.addAttribute("cropImageform", cropImageform);
		
		return "crops/new";
		
	}

	// formから送られた作物データを登録
	@RequestMapping(value = "/crop", method = RequestMethod.POST)
	public String create(Principal principal, @Validated @ModelAttribute("cropform") CropForm form,
			BindingResult result, Model model, RedirectAttributes redirAttrs, RedirectAttributes attributes)
			throws IOException {

		// 同一の作物が既に登録されている場合、エラー項目に追加
		if (repository.findByName(form.getName()) != null) {
			FieldError fieldError = new FieldError(result.getObjectName(), "name", "その作物はすでに登録されています。");
			result.addError(fieldError);
		}
		 //エラーがある場合、エラー文を表示し、新しいformを送信
		if (result.hasErrors()) {
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-danger");
			model.addAttribute("message", "作物データ登録に失敗しました。");
			return "crops/index";
		}

		// CropEntityのインスタンスを生成
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
		Iterable<CropImage> imageList = imageRepository.findAllByCropIdOrderByUpdatedAtDesc(form.getId());
		model.addAttribute("list", imageList);

		model.addAttribute("hasMessage", true);
		model.addAttribute("class", "alert-info");
		model.addAttribute("message", "作物データ登録に成功しました。");
		//attributes.addFlashAttribute("cropId", form.getId());
		return "crops/index";
		
		
	}

	// 画像の保存
	@RequestMapping(value = "/crops/upload-image", method = RequestMethod.POST)
	public String uploadImage(Principal principal, @Validated @ModelAttribute("cropImageform") CropImageForm form,
			BindingResult result, Model model, @RequestParam MultipartFile image,
			RedirectAttributes redirAttrs) throws IOException {
		// ファイルの拡張子が画像形式かどうかの検証をエラー項目に追加
		if (image != null && !isImageFile(image)) {
			FieldError fieldError = new FieldError(result.getObjectName(), "image", "画像ファイル以外はアップロードできません。");
			result.addError(fieldError);
		}
		// エラーがある場合、エラー文を表示し、新しいformを送信
		if (result.hasErrors()) {
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-danger");
			model.addAttribute("message", "画像のアップロードに失敗しました。");
			return "redirect:/crops/new";
		}
		// CropImagEntityのインスタンスを生成
		CropImage imageEntity = new CropImage();
		Path uploadPath = Path.of(UPLOAD_DIR);
		Path filePath = uploadPath.resolve(image.getOriginalFilename());
		imageEntity.setCropId(form.getCropId());
		imageEntity.setPath(filePath.toString());
		saveFile(image);
		// entityの保存
		imageRepository.saveAndFlush(imageEntity);
		Iterable<CropImage> imageList = imageRepository.findAllByCropIdOrderByUpdatedAtDesc(form.getCropId());
		model.addAttribute("list", imageList);

		redirAttrs.addFlashAttribute("hasMessage", true);
		redirAttrs.addFlashAttribute("class", "alert-info");
		redirAttrs.addFlashAttribute("message", "画像のアップロードに成功しました。");
		return "/crops/new";
	}

	private boolean isImageFile(MultipartFile file) {
		// 拡張子が一般的な画像形式かどうかの検証。
		String fileName = file.getOriginalFilename();
		return fileName != null
				&& (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png"));
	}

	private void saveFile(MultipartFile image) throws IOException {
		Path uploadPath = Path.of(UPLOAD_DIR);

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

	// 画像の削除
	@RequestMapping(value = "/crops/delete-image", method = RequestMethod.POST)
	public String deleteImage(@RequestParam("imageId") Long imageId, BindingResult result,
			RedirectAttributes redirAttrs, Model model) throws IOException {
		Optional<CropImage> entity = imageRepository.findById(imageId);
		// 画面が保持されるか要確認
		if (entity == null) {
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-danger");
			model.addAttribute("message", "その画像は存在しません。");
			return "redirect:/crops/new";
		}
		imageRepository.deleteById(imageId);
		redirAttrs.addFlashAttribute("hasMessage", true);
		redirAttrs.addFlashAttribute("class", "alert-info");
		redirAttrs.addFlashAttribute("message", "画像の削除に成功しました。");
		return "redirect:/crops/new";
	}

	// 作物の一覧表示画面ｎ表示
	@GetMapping(path = "/crops/list")
	public String showList(Principal principal, Model model) throws IOException {
		// User情報の取得
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		// 該当のユーザーのすべての作物のリストを作成
		Iterable<Crop> list = repository.findAllByUserIdOrderByUpdatedAtDesc(user.getUserId());

		return "/crops/list";
	}

	// 作物検索機能
	@GetMapping(path = "/crops/search")
	public List<Crop> searchCrops(@RequestParam String keyword) {
		return repository.findByNameContaining(keyword);
	}

	@GetMapping(path = "/crops/detail")
	public String showDetai(@ModelAttribute("cropId") Long cropId, Model model) throws IOException {
		Optional<Crop> entity = repository.findById(cropId);
		model.addAttribute("crop", entity);
		Iterable<CropImage> imageList = imageRepository.findAllByCropIdOrderByUpdatedAtDesc(cropId);
		model.addAttribute("list", imageList);
		return "/crops/detail";
	}

	// 編集画面
	@GetMapping(path = "/crops/edit")
	public String showEditPage(@ModelAttribute("cropId") Long cropId, Model model) throws IOException {
		Optional<Crop> entity = repository.findById(cropId);
		CropForm form = getCrop(entity);
		model.addAttribute("form", form);
		Iterable<CropImage> imageList = imageRepository.findAllByCropIdOrderByUpdatedAtDesc(form.getId());
		model.addAttribute("list", imageList);
		return "/crops/edit";
		// @Validated @ModelAttribute("cropImageform") CropImageForm form, BindingResult
		// result,
		// Model model, @RequestParam("image") MultipartFile image, RedirectAttributes
		// redirAttrs )
	}

	public CropForm getCrop(Optional<Crop> entity) throws IOException {
		modelMapper.getConfiguration().setAmbiguityIgnored(true);
		modelMapper.typeMap(Crop.class, CropForm.class);
		CropForm form = modelMapper.map(entity, CropForm.class);
		return form;
	}

	// 編集データの送信
	@RequestMapping(value = "/crops/edit-complete", method = RequestMethod.POST)
	public String edit(@RequestParam("cropId") Long cropId, @Validated @ModelAttribute("cropform") CropForm form,
			BindingResult result, Model model, RedirectAttributes redirAttrs, RedirectAttributes attributes)
			throws IOException {
		Optional<Crop> entity = repository.findById(cropId);
		// 同一の作物が既に登録されている場合、エラー項目に追加
		if (repository.findByName(form.getName()) != null) {
			FieldError fieldError = new FieldError(result.getObjectName(), "email", "その作物はすでに登録されています。");
			result.addError(fieldError);
		}
		// エラーがある場合、エラー文を表示し、新しいformを送信
		if (result.hasErrors()) {
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-danger");
			model.addAttribute("message", "作物データ編集に失敗しました。");
			// データを保持する処理

			CropForm oldForm = getCrop(entity);
			model.addAttribute("form", oldForm);
			Iterable<CropImage> imageList = imageRepository.findAllByCropIdOrderByUpdatedAtDesc(oldForm.getId());
			model.addAttribute("list", imageList);
			return "redirect:/crops/edit";
		}

		// CropEntityのフィールド値を更新
		entity.ifPresent(crop -> crop.setName(form.getName()));
		entity.ifPresent(crop -> crop.setSowing_start(form.getSowing_start()));
		entity.ifPresent(crop -> crop.setSowing_end(form.getSowing_end()));
		entity.ifPresent(crop -> crop.setHarvest_start(form.getHarvest_start()));
		entity.ifPresent(crop -> crop.setHarvest_end(form.getHarvest_end()));
		entity.ifPresent(crop -> crop.setName(form.getName()));
		entity.ifPresent(crop -> crop.setCultivationp_period(form.getCultivationp_period()));
		// entityの保存
		repository.saveAndFlush(entity);
		// 画像を表示するためのimageListを作成
		Iterable<CropImage> imageList = imageRepository.findAllByCropIdOrderByUpdatedAtDesc(form.getId());
		model.addAttribute("list", imageList);

		redirAttrs.addFlashAttribute("hasMessage", true);
		redirAttrs.addFlashAttribute("class", "alert-info");
		redirAttrs.addFlashAttribute("message", "作物データ登録に成功しました。");
		attributes.addFlashAttribute("cropId", form.getId());
		return "redirect:/crops/detail";

	}
	@RequestMapping(value = "/crops/delete", method = RequestMethod.POST)
	public String delete(@RequestParam("cropId") Long cropId, BindingResult result, RedirectAttributes redirAttrs,
			Model model) throws IOException {
		Optional<Crop> entity = repository.findById(cropId);
		if (entity == null) {
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-danger");
			model.addAttribute("message", "その作物データは存在しません。");
			return "/crops/list";
		}
		repository.deleteById(cropId);
		redirAttrs.addFlashAttribute("hasMessage", true);
		redirAttrs.addFlashAttribute("class", "alert-info");
		redirAttrs.addFlashAttribute("message", "作物データの削除に成功しました。");
		return "/crops/list";
	}
}
