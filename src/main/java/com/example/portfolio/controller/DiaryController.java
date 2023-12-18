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
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
import org.springframework.web.bind.annotation.DeleteMapping;
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
import com.example.portfolio.repository.DiaryImageReposiory;
import com.example.portfolio.repository.DiaryRepository;
import com.example.portfolio.repository.PlanRepository;
import com.example.portfolio.repository.CropImageReposiory;
import com.example.portfolio.entity.Crop;
import com.example.portfolio.entity.CropImage;
import com.example.portfolio.entity.Diary;
import com.example.portfolio.entity.DiaryImage;
import com.example.portfolio.entity.Plan;
import com.example.portfolio.entity.Section;
import com.example.portfolio.entity.UserInf;
import com.example.portfolio.form.CropForm;
import com.example.portfolio.form.CropImageForm;
import com.example.portfolio.form.DiaryForm;
import com.example.portfolio.form.PlanForm;
import com.example.portfolio.form.UserForm;

import org.springframework.beans.BeanUtils;

@Controller

public class DiaryController {

	protected static Logger log = LoggerFactory.getLogger(DiaryController.class);

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	DiaryRepository repository;

	@Autowired
	DiaryImageReposiory imageRepository;

	@Autowired
	PlanRepository planRepository;

	private static final String UPLOAD_DIR = "uploads";

	// 作物データ新規登録formの表示
	@GetMapping(path = "/diarys/new/{planId}")
	public String newDiary(@PathVariable Long planId, Model model) {
		DiaryForm form = new DiaryForm();
		form.setPlanId(planId);
		model.addAttribute("form", form);
		return "diarys/new";
	}

// formから送られたデータを登録
	@RequestMapping(value = "/diary", method = RequestMethod.POST)
	public String create(Principal principal, @RequestParam("images") MultipartFile[] images,
			@Validated @ModelAttribute("form") DiaryForm form, BindingResult result, Model model) throws IOException {
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		// ファイルの拡張子が画像形式かどうかの検証をエラー項目に追加
		if (images != null && images.length > 0 && !images[0].isEmpty()) {
			for (MultipartFile image : images) {
				if (image != null && !isImageFile(image)) {
					model.addAttribute("imageError", "画像ファイル形式が正しくありません。");
				}
			}
		}
		if (form.getPlanId() == null || planRepository.findById(form.getPlanId()) == null) {
			FieldError fieldError = new FieldError(result.getObjectName(), "planId", "栽培計画が存在しません。");
			result.addError(fieldError);
		}
		if (result.hasErrors()) {
			model.addAttribute("form", form);
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-danger");
			model.addAttribute("message", "栽培日誌登録に失敗しました。");
			return "diarys/new";
		}

		// DiaryEntityのインスタンスを生成
		Diary entity = new Diary();
		entity.setUserId(user.getUserId());
		entity.setPlanId(form.getPlanId());
		entity.setRecord_date(form.getRecord_date());
		entity.setDescription(form.getDescription());
		// entityの保存
		repository.saveAndFlush(entity);

		LocalDateTime currentDateTime = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
		String formattedDateTime = currentDateTime.format(formatter);
		if (images != null && images.length > 0 && !images[0].isEmpty()) {
			Path uploadPath = Path.of("images", UPLOAD_DIR);
			for (MultipartFile image : images) {
				DiaryImage imageEntiy = new DiaryImage();
				Path filePath = uploadPath.resolve(formattedDateTime + image.getOriginalFilename());
				imageEntiy.setDiaryId(entity.getId());
				imageEntiy.setPath("/" + filePath.toString().replace("\\", "/"));
				saveFile(image, formattedDateTime);
				imageRepository.saveAndFlush(imageEntiy);
			}
		}
		model.addAttribute("hasMessage", true);
		model.addAttribute("class", "alert-info");
		model.addAttribute("message", "栽培日誌登録に成功しました。");
		return "redirect:/diarys/detail/" + entity.getId();

	}

	private boolean isImageFile(MultipartFile file) {
		// 拡張子が一般的な画像形式かどうかの検証。
		String fileName = file.getOriginalFilename();
		return fileName != null
				&& (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png"));
	}

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

	@GetMapping(path = "/diarys/detail/{diaryId}")
	public String showDetail(@PathVariable Long diaryId, Model model) throws IOException {
		Optional<Diary> optionalDiary = repository.findById(diaryId);
		Diary diary = optionalDiary.orElseThrow(() -> new RuntimeException("Crop not found")); // もし Optional // // //
																								// が空の場合は例外をスローするなどの対処
		model.addAttribute("diary", diary);
		List<DiaryImage> imageList = imageRepository.findAllByDiaryIdOrderByUpdatedAtDesc(diaryId);
		model.addAttribute("list", imageList);
		return "diarys/detail";
	}

	// 編集画面
	@GetMapping(path = "/diarys/edit/{diaryId}")
	public String showEditPage(@PathVariable Long diaryId, Model model) throws IOException {
		Optional<Diary> optionalDiary = repository.findById(diaryId);
		Diary diary = optionalDiary.orElseThrow(() -> new RuntimeException("Crop not found")); // もし Optional
		DiaryForm form = getDiary(diary);
		List<DiaryImage> imageList = imageRepository.findAllByDiaryIdOrderByUpdatedAtDesc(diaryId);
		model.addAttribute("form", form);
		model.addAttribute("list", imageList);
		model.addAttribute("planId", diary.getPlanId());
		return "diarys/edit";
	}

	public DiaryForm getDiary(Diary diary) throws IOException {
		modelMapper.getConfiguration().setAmbiguityIgnored(true);
		modelMapper.typeMap(Diary.class, DiaryForm.class).addMapping(Diary::getPlanId, DiaryForm::setPlanId);
		DiaryForm form = modelMapper.map(diary, DiaryForm.class);

		return form;
	}

	@GetMapping("/diarys/delete-image")
	public String deleteImage(@RequestParam Long imageId, @RequestParam Long diaryId, Model model,
			RedirectAttributes attributes) throws IOException {
		imageRepository.deleteById(imageId);
		attributes.addFlashAttribute("hasMessage", true);
		attributes.addFlashAttribute("class", "alert-info");
		attributes.addFlashAttribute("message", "画像の削除に成功しました。");
		return "redirect:/diarys/edit/" + diaryId;
	}

	// 編集データの送信
	@RequestMapping(value = "/diarys/edit-complete", method = RequestMethod.POST)
	public String edit(Principal principal, @RequestParam("images") MultipartFile[] images,
			@Validated @ModelAttribute("form") DiaryForm form, BindingResult result, Model model,
			RedirectAttributes attributes) throws IOException {
		Optional<Diary> optionalDiary = repository.findById(form.getId());
		Diary diary = optionalDiary.orElseThrow(() -> new RuntimeException("Crop not found")); // もし Optional
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		// ファイルの拡張子が画像形式かどうかの検証をエラー項目に追加
		if (images != null && images.length > 0 && !images[0].isEmpty()) {
			for (MultipartFile image : images) {
				if (image != null && !isImageFile(image)) {
					model.addAttribute("imageError", "画像ファイル形式が正しくありません。");
				}
			}
		}
		if (form.getPlanId() == null || planRepository.findById(form.getPlanId()) == null) {
			FieldError fieldError = new FieldError(result.getObjectName(), "planId", "栽培計画が存在しません。");
			result.addError(fieldError);
		}
		if (result.hasErrors()) {
			model.addAttribute("form", form);
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-danger");
			model.addAttribute("message", "栽培日誌編集に失敗しました。");
			return "diarys/edit";
		}

		// DiaryEntityのインスタンスを生成
		diary.setUserId(user.getUserId());
		diary.setPlanId(form.getPlanId());
		diary.setRecord_date(form.getRecord_date());
		diary.setDescription(form.getDescription());
		// entityの保存
		repository.saveAndFlush(diary);

		LocalDateTime currentDateTime = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
		String formattedDateTime = currentDateTime.format(formatter);
		if (images != null && images.length > 0 && !images[0].isEmpty()) {
			Path uploadPath = Path.of("images", UPLOAD_DIR);
			for (MultipartFile image : images) {
				DiaryImage imageEntiy = new DiaryImage();
				Path filePath = uploadPath.resolve(formattedDateTime + image.getOriginalFilename());
				imageEntiy.setDiaryId(diary.getId());
				imageEntiy.setPath("/" + filePath.toString().replace("\\", "/"));
				saveFile(image, formattedDateTime);
				imageRepository.saveAndFlush(imageEntiy);
			}
		}
		model.addAttribute("hasMessage", true);
		model.addAttribute("class", "alert-info");
		model.addAttribute("message", "栽培日誌編集に成功しました。");
		return "redirect:/diarys/detail/" + diary.getId();
	}

	@GetMapping(path = "/diarys/delete/{diaryId}")
	public String delete(@PathVariable Long diaryId, RedirectAttributes redirAttrs, Model model) throws IOException {
		repository.deleteById(diaryId);
		redirAttrs.addFlashAttribute("hasMessage", true);
		redirAttrs.addFlashAttribute("class", "alert-info");
		redirAttrs.addFlashAttribute("message", "栽培日誌の削除に成功しました。");
		return "redirect:/diarys/list";
	}

	// 作物一覧画面表示
	@GetMapping(path = "/diarys/list")
	public String showList(Principal principal, Model model) throws IOException {
		// User情報の取得
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		// 該当のユーザーのすべての作物のリストを作成
		List<Diary> list = repository.findAllByUserIdOrderByUpdatedAtDesc(user.getUserId());
		model.addAttribute("list", list);
		return "/diarys/list";
	}

	// 作物検索機能
	@RequestMapping(value = "/diarys/search", method = RequestMethod.POST)
	public String searchCrops(Principal principal,
			@RequestParam(name = "start_date", required = false) LocalDate start_date,
			@RequestParam(name = "end_date", required = false) LocalDate end_date, Model model,
			RedirectAttributes redirAttrs) {
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		List<Diary> list = repository.findAllByUserIdOrderByUpdatedAtDesc(user.getUserId());
		List<Diary> newList = new ArrayList<>();
		if (start_date != null && end_date != null) {
			for (Diary diary : list) {
				if (diary.getRecord_date().isAfter(start_date.minusDays(1)) && diary.getRecord_date().isBefore(end_date.plusDays(1))) {
					newList.add(diary);
				}
			}
		}
		if (start_date != null && end_date == null) {
			for (Diary diary : list) {
				if (diary.getRecord_date().isAfter(start_date.minusDays(1))) {
					newList.add(diary);
				}
			}
		}
		if (start_date == null && end_date != null) {
			for (Diary diary : list) {
				if (diary.getRecord_date().isBefore(end_date.plusDays(1))) {
					newList.add(diary);
				}
			}
		}
		if (start_date == null && end_date == null) {
			newList = list;
		}
		if (newList.isEmpty()) {
			model.addAttribute("message", "その栽培日誌は見つかりませんでした。");
		}
		model.addAttribute("list", newList);
		model.addAttribute("start_date", start_date);
		model.addAttribute("end_date", end_date);
		return "/diarys/list";
	}
}
