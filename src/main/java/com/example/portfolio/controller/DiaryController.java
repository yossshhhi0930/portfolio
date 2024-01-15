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

	@Value("${upload.path}")
	private String UPLOAD_DIR;
	
	@Value("${set.path}")
	private String SET_PATH;

	// 栽培日誌登録画面表示
	@GetMapping(path = "/diarys/new/{planId}")
	public String newDiary(@PathVariable Long planId, Model model) {
		DiaryForm form = new DiaryForm();
		form.setPlanId(planId);
		model.addAttribute("form", form);
		return "diarys/new";
	}

	// 栽培日誌登録
	@PostMapping(path = "/diary")
	public String create(Principal principal, @RequestParam("images") MultipartFile[] images,
			@Validated @ModelAttribute("form") DiaryForm form, BindingResult result, Model model) throws IOException {
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		Long userId = user.getUserId();
		Long planId = form.getPlanId();
		LocalDate record_date = form.getRecord_date();
		String description = form.getDescription();
		// formのフィールドエラー以外のエラー（今回は画像のエラー）リストのカウンター
		int imageErrorCount = 0;
		// formのフィールドエラー以外のエラーリストを生成
		List<String> messagelist = new ArrayList<>();
		// 画像ファイルの拡張子が画像形式でない場合に返すエラーの追加
		if (images != null && images.length > 0 && !images[0].isEmpty()) {
			for (MultipartFile image : images) {
				if (image != null && !isImageFile(image)) {
					model.addAttribute("imageError", "画像ファイル形式が正しくありません。");
					messagelist.add("画像ファイル形式が正しくありません。");
					imageErrorCount++;
				}
			}
		}
		// 栽培計画のIdが未入力、またはIdが誤っている場合に返すエラーの追加
		if (planId == null || planRepository.findById(planId) == null) {
			FieldError fieldError = new FieldError(result.getObjectName(), "planId", "栽培計画が存在しません。");
			result.addError(fieldError);
		}
		if (imageErrorCount > 0 || result.hasErrors()) {
			model.addAttribute("notFieldMessages", messagelist);
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-danger");
			model.addAttribute("message", "栽培日誌の登録に失敗しました。");
			return "diarys/new";
		}
		Diary entity = new Diary(userId, planId, record_date, description);
		repository.saveAndFlush(entity);
		String formattedDateTime = getFormattedDateTime();
		Path uploadPath = Path.of(SET_PATH);
		if (images != null && images.length > 0 && !images[0].isEmpty()) {
			for (MultipartFile image : images) {
				Path filePath = uploadPath.resolve(formattedDateTime + image.getOriginalFilename());
				Long diaryId = entity.getId();
				String path = StringUtils.cleanPath("/" + filePath.toString());
				saveFile(image, formattedDateTime);
				DiaryImage imageEntiy = new DiaryImage(diaryId, path);
				imageRepository.saveAndFlush(imageEntiy);
			}
		}
		model.addAttribute("hasMessage", true);
		model.addAttribute("class", "alert-info");
		model.addAttribute("message", "栽培日誌の登録が完了しました。");
		return "redirect:/diarys/detail/" + entity.getId();
	}

	// 栽培日誌詳細表示
	@GetMapping(path = "/diarys/detail/{diaryId}")
	public String showDetail(@PathVariable Long diaryId, Model model) throws IOException {
		Optional<Diary> optionalDiary = repository.findById(diaryId);
		Diary entity = optionalDiary.orElseThrow(() -> new RuntimeException("Diary not found"));
		List<DiaryImage> imageList = imageRepository.findAllByDiaryIdOrderByUpdatedAtAsc(diaryId);
		model.addAttribute("diary", entity);
		model.addAttribute("list", imageList);
		return "diarys/detail";
	}

	// 栽培日誌編集画面表示
	@GetMapping(path = "/diarys/edit/{diaryId}")
	public String showEditPage(@PathVariable Long diaryId, Model model) throws IOException {
		Optional<Diary> optionalDiary = repository.findById(diaryId);
		Diary entity = optionalDiary.orElseThrow(() -> new RuntimeException("Diary not found"));
		DiaryForm form = getDiaryForm(entity);
		List<DiaryImage> imageList = imageRepository.findAllByDiaryIdOrderByUpdatedAtAsc(diaryId);
		model.addAttribute("form", form);
		model.addAttribute("list", imageList);
		model.addAttribute("planId", entity.getPlanId());
		return "diarys/edit";
	}

	// 栽培日誌画像削除
	@GetMapping("/diarys/delete-image")
	public String deleteImage(@RequestParam Long imageId, @RequestParam Long diaryId, Model model,
			RedirectAttributes attributes) throws IOException {
		imageRepository.deleteById(imageId);
		attributes.addFlashAttribute("hasMessage", true);
		attributes.addFlashAttribute("class", "alert-info");
		attributes.addFlashAttribute("message", "画像の削除が完了しました。");
		return "redirect:/diarys/edit/" + diaryId;
	}

	// 栽培日誌編集
	@PostMapping(path = "/diarys/edit-complete")
	public String edit(@RequestParam("images") MultipartFile[] images,
			@Validated @ModelAttribute("form") DiaryForm form, BindingResult result, Model model,
			RedirectAttributes redirAttrs) throws IOException {
		Long planId = form.getPlanId();
		LocalDate record_date = form.getRecord_date();
		String description = form.getDescription();
		Optional<Diary> optionalDiary = repository.findById(form.getId());
		Diary entity = optionalDiary.orElseThrow(() -> new RuntimeException("Diary not found"));
		List<String> messagelist = new ArrayList<>();
		int imageErrorCount = 0;
		if (images != null && images.length > 0 && !images[0].isEmpty()) {
			for (MultipartFile image : images) {
				if (image != null && !isImageFile(image)) {
					model.addAttribute("imageError", "画像ファイル形式が正しくありません。");
					messagelist.add("画像ファイル形式が正しくありません。");
					imageErrorCount++;
				}
			}
		}
		// 栽培計画のIdが未入力、またはIdが誤っている場合に返すエラーの追加
		if (planId == null || planRepository.findById(planId) == null) {
			FieldError fieldError = new FieldError(result.getObjectName(), "planId", "栽培計画が存在しません。");
			result.addError(fieldError);
		}
		if (imageErrorCount > 0 || result.hasErrors()) {
			model.addAttribute("notFieldMessages", messagelist);
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-danger");
			model.addAttribute("message", "栽培日誌の編集に失敗しました。");
			return "diarys/edit";
		}
		entity.setPlanId(planId);
		entity.setRecord_date(record_date);
		entity.setDescription(description);
		repository.saveAndFlush(entity);
		String formattedDateTime = getFormattedDateTime();
		if (images != null && images.length > 0 && !images[0].isEmpty()) {
			Path uploadPath = Path.of(SET_PATH);
			for (MultipartFile image : images) {
				DiaryImage imageEntiy = new DiaryImage();
				Path filePath = uploadPath.resolve(formattedDateTime + image.getOriginalFilename());
				imageEntiy.setDiaryId(entity.getId());
				imageEntiy.setPath(StringUtils.cleanPath("/" + filePath.toString()));
				saveFile(image, formattedDateTime);
				imageRepository.saveAndFlush(imageEntiy);
			}
		}
		redirAttrs.addFlashAttribute("hasMessage", true);
		redirAttrs.addFlashAttribute("class", "alert-info");
		redirAttrs.addFlashAttribute("message", "栽培日誌の編集が完了しました。");
		return "redirect:/diarys/detail/" + entity.getId();
	}

	// 栽培日誌削除
	@GetMapping(path = "/diarys/delete/{diaryId}")
	public String delete(@PathVariable Long diaryId, Model model, RedirectAttributes redirAttrs) throws IOException {
		repository.deleteById(diaryId);
		redirAttrs.addFlashAttribute("hasMessage", true);
		redirAttrs.addFlashAttribute("class", "alert-info");
		redirAttrs.addFlashAttribute("message", "栽培日誌の削除が完了しました。");
		return "redirect:/diarys/list";
	}

	// 栽培日誌一覧表示
	@GetMapping(path = "/diarys/list")
	public String showList(Principal principal, Model model) throws IOException {
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		List<Diary> list = repository.findAllByUserIdOrderByUpdatedAtAsc(user.getUserId());
		model.addAttribute("list", list);
		return "/diarys/list";
	}

	// 栽培日誌検索
	@PostMapping(path = "/diarys/search")
	public String searchCrops(Principal principal,
			@RequestParam(name = "start_date", required = false) LocalDate start_date,
			@RequestParam(name = "end_date", required = false) LocalDate end_date, Model model,
			RedirectAttributes redirAttrs) {
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		List<Diary> list = repository.findAllByUserIdOrderByUpdatedAtAsc(user.getUserId());
		List<Diary> newList = new ArrayList<>();
		if (start_date != null && end_date != null) {
			for (Diary diary : list) {
				if (diary.getRecord_date().isAfter(start_date.minusDays(1))
						&& diary.getRecord_date().isBefore(end_date.plusDays(1))) {
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
			model.addAttribute("sarchMessage", "その栽培日誌は見つかりませんでした。");
		}
		model.addAttribute("list", newList);
		model.addAttribute("start_date", start_date);
		model.addAttribute("end_date", end_date);
		return "/diarys/list";
	}

	// DiaryエンティティからDiaryFormエンティティの取得
	public DiaryForm getDiaryForm(Diary diary) throws IOException {
		modelMapper.getConfiguration().setAmbiguityIgnored(true);
		modelMapper.typeMap(Diary.class, DiaryForm.class).addMapping(Diary::getPlanId, DiaryForm::setPlanId);
		DiaryForm form = modelMapper.map(diary, DiaryForm.class);
		return form;
	}

	// 拡張子が一般的な画像形式かどうかの検証
	private boolean isImageFile(MultipartFile file) {
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
		Path uploadPath = Path.of(UPLOAD_DIR);
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
