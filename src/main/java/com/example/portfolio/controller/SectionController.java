package com.example.portfolio.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.portfolio.entity.Crop;
import com.example.portfolio.entity.CropImage;
import com.example.portfolio.entity.Section;
import com.example.portfolio.entity.UserInf;
import com.example.portfolio.form.CropForm;
import com.example.portfolio.form.SectionForm;
import com.example.portfolio.repository.CropRepository;
import com.example.portfolio.repository.SectionRepository;

@Controller
public class SectionController {

	protected static Logger log = LoggerFactory.getLogger(CropController.class);

	@Autowired
	SectionRepository repository;

	@Autowired
	private ModelMapper modelMapper;

//区画データ新規登録formの表示
	@GetMapping(path = "/sections/new")
	public String newSection(Model model) {
		SectionForm section = new SectionForm();
		model.addAttribute("form", section);
		return "sections/new";

	}

	// formから送られた区画データを登録
	@RequestMapping(value = "/section", method = RequestMethod.POST)
	public String create(Principal principal, @Validated @ModelAttribute("form") SectionForm form, BindingResult result,
			Model model, RedirectAttributes attributes) throws IOException {
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();

		// 同一の作物が既に登録されている場合、エラー項目に追加
		if (repository.findByNameAndUserId(form.getName(), user.getUserId()) != null) {
			FieldError fieldError = new FieldError(result.getObjectName(), "name", "その区画名は使用できません。");
			result.addError(fieldError);
		}
		// エラーがある場合、エラー文を表示しformを再度返す

		if (result.hasErrors()) {
			model.addAttribute("form", form);
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-danger");
			model.addAttribute("message", "	区画登録に失敗しました。");
			return "sections/new";
		}

		// SectionEntityのインスタンスを生成
		Section entity = new Section();

		entity.setUserId(user.getUserId());
		entity.setName(form.getName());
		entity.setDescription(form.getDescription());

		// entityの保存
		repository.saveAndFlush(entity);
		// 画像を表示するたmodelめのimageListを作成
		attributes.addFlashAttribute("hasMessage", true);
		attributes.addFlashAttribute("class", "alert-info");
		attributes.addFlashAttribute("message", "区画登録に成功しました。");
		return "redirect:sections/detail/" + entity.getId();
	}

	@GetMapping(path = "/sections/detail/{sectionId}")
	public String showDetail(@PathVariable Long sectionId, Model model) throws IOException {
		Optional<Section> optionalSection = repository.findById(sectionId);
		Section section = optionalSection.orElseThrow(() -> new RuntimeException("Section not found")); // もし Optional
																										// // //
																										// が空の場合は例外をスローするなどの対処
		model.addAttribute("section", section);
		return "sections/detail";
	}

	// 編集画面
	@GetMapping(path = "/sections/edit/{sectionId}")
	public String showEditPage(@PathVariable Long sectionId, Model model) throws IOException {
		Optional<Section> optionalSection = repository.findById(sectionId);
		Section section = optionalSection.orElseThrow(() -> new RuntimeException("Section not found")); // もし Optional
																										// // //
																										// が空の場合は例外をスローするなどの対処
		SectionForm form = getSection(section);
		model.addAttribute("form", form);
		return "sections/edit";
	}

	public SectionForm getSection(Section section) throws IOException {
		modelMapper.getConfiguration().setAmbiguityIgnored(true);
		// modelMapper.typeMap(Section.class, SectionForm.class);
		SectionForm form = modelMapper.map(section, SectionForm.class);
		return form;
	}

	// 編集データの送信
	@RequestMapping(value = "/sections/edit-complete", method = RequestMethod.POST)
	public String edit(Principal principal, @Validated @ModelAttribute("form") SectionForm form, BindingResult result,
			Model model, RedirectAttributes attributes) throws IOException {
		Optional<Section> optionalSection = repository.findById(form.getId());
		Section section = optionalSection.orElseThrow(() -> new RuntimeException("Section not found")); // もし Optional
																										// // //
																										// が空の場合は例外をスローするなどの対処
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		// 名前を変更し、他に同一の名前の区画が存在する場合、エラーを返す
		if (!section.getName().equals(form.getName())
				&& repository.findByNameAndUserId(form.getName(), user.getUserId()) != null) {
			FieldError fieldError = new FieldError(result.getObjectName(), "name", "その区画名は使用できません。");
			result.addError(fieldError);
		}
		// エラーがある場合、エラー文を表示し、新しいformを送信
		if (result.hasErrors()) {
			model.addAttribute("form", form);
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-danger");
			model.addAttribute("message", "区画の編集に失敗しました。");
			// データを保持する処理
			return "/sections/edit";
		}
		// SectionsEntityのフィールド値を更新
		section.setName(form.getName());
		section.setDescription(form.getDescription());
		// entityの保存
		repository.saveAndFlush(section);
		// 画像を表示するためのimageListを作成
		attributes.addFlashAttribute("hasMessage", true);
		attributes.addFlashAttribute("class", "alert-info");
		attributes.addFlashAttribute("message", "区画の編集に成功しました。");
		return "redirect:/sections/detail/" + form.getId();
	}

	@GetMapping(path = "/sections/delete/{sectionId}")
	public String delete(Principal principal, @PathVariable Long sectionId, Model model, RedirectAttributes redirAttrs)
			throws IOException {
		// User情報の取得
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		repository.deleteById(sectionId);
		Iterable<Section> list = repository.findAllByUserIdOrderByUpdatedAtDesc(user.getUserId());
		model.addAttribute("list", list);
		redirAttrs.addFlashAttribute("hasMessage", true);
		redirAttrs.addFlashAttribute("class", "alert-info");
		redirAttrs.addFlashAttribute("message", "区画の削除に成功しました。");
		return "/sections/list";
	}

	// 区画一覧画面の表示
	@GetMapping(path = "/sections/list")
	public String showList(Principal principal, Model model) throws IOException {
		// User情報の取得
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		// 該当のユーザーのすべての作物のリストを作成
		Iterable<Section> list = repository.findAllByUserIdOrderByUpdatedAtDesc(user.getUserId());
		model.addAttribute("list", list);
		return "/sections/list";
	}

	// 区画検索機能
	@RequestMapping(value = "/sections/search", method = RequestMethod.POST)
	public String searchCrops(Principal principal, @RequestParam(name = "keyword", required = false) String keyword,
			Model model, RedirectAttributes redirAttrs) {
		// User情報の取得
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		List<Section> list = new ArrayList<>();
		if (keyword == null) {
			list = repository.findAllByUserIdOrderByUpdatedAtDesc(user.getUserId());
		}
		if (keyword != null) {
			list = repository.findByNameContainingAndUserId(keyword, user.getUserId());
		}
		if (list.isEmpty()) {
			model.addAttribute("message", "その区画は見つかりませんでした。");
		}
		model.addAttribute("list", list);
		model.addAttribute("keyword", keyword);
		return "/sections/list";

	}

}