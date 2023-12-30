package com.example.portfolio.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.example.portfolio.entity.Section;
import com.example.portfolio.entity.UserInf;
import com.example.portfolio.form.SectionForm;
import com.example.portfolio.repository.SectionRepository;

@Controller
public class SectionController {

	protected static Logger log = LoggerFactory.getLogger(CropController.class);

	@Autowired
	SectionRepository repository;

	@Autowired
	private ModelMapper modelMapper;

	// 区画登録画面表示
	@GetMapping(path = "/sections/new")
	public String newSection(Model model) {
		SectionForm form = new SectionForm();
		model.addAttribute("form", form);
		return "sections/new";
	}

	// 区画登録
	@PostMapping(path = "/section")
	public String create(Principal principal, @Validated @ModelAttribute("form") SectionForm form, BindingResult result,
			Model model, RedirectAttributes attributes) throws IOException {
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		Long userId = user.getUserId();
		String name = form.getName();
		String description = form.getDescription();
		// 同一の作物が既に登録されている場合に返すエラーの追加
		if (repository.findByNameAndUserId(name, userId) != null) {
			FieldError fieldError = new FieldError(result.getObjectName(), "name", "その区画名は既に登録されています。");
			result.addError(fieldError);
		}
		if (result.hasErrors()) {
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-danger");
			model.addAttribute("message", "	区画の登録に失敗しました。");
			return "sections/new";
		}
		Section entity = new Section(userId, name, description);
		repository.saveAndFlush(entity);
		attributes.addFlashAttribute("hasMessage", true);
		attributes.addFlashAttribute("class", "alert-info");
		attributes.addFlashAttribute("message", "区画の登録が完了しました。");
		return "redirect:sections/detail/" + entity.getId();
	}

	// 区画詳細表示
	@GetMapping(path = "/sections/detail/{sectionId}")
	public String showDetail(@PathVariable Long sectionId, Model model) throws IOException {
		Optional<Section> optionalSection = repository.findById(sectionId);
		Section entity = optionalSection.orElseThrow(() -> new RuntimeException("Section not found"));
		model.addAttribute("section", entity);
		return "sections/detail";
	}

	// 区画編集画面表示
	@GetMapping(path = "/sections/edit/{sectionId}")
	public String showEditPage(@PathVariable Long sectionId, Model model) throws IOException {
		Optional<Section> optionalSection = repository.findById(sectionId);
		Section entity = optionalSection.orElseThrow(() -> new RuntimeException("Section not found"));
		SectionForm form = getSectionForm(entity);
		model.addAttribute("form", form);
		return "sections/edit";
	}

	// 区画編集
	@PostMapping(path = "/sections/edit-complete")
	public String edit(Principal principal, @Validated @ModelAttribute("form") SectionForm form, BindingResult result,
			Model model, RedirectAttributes attributes) throws IOException {
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		Long userId = user.getUserId();
		Long id = form.getId();
		String name = form.getName();
		String description = form.getDescription();
		Optional<Section> optionalSection = repository.findById(id);
		Section entity = optionalSection.orElseThrow(() -> new RuntimeException("Section not found"));
		// 名前を変更し、同一の作物が既に登録されている場合に返すエラーの追加
		if (!entity.getName().equals(name) && repository.findByNameAndUserId(name, userId) != null) {
			FieldError fieldError = new FieldError(result.getObjectName(), "name", "その区画名は既に登録されています。");
			result.addError(fieldError);
		}
		if (result.hasErrors()) {
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-danger");
			model.addAttribute("message", "区画の編集に失敗しました。");
			return "/sections/edit";
		}
		entity.setName(name);
		entity.setDescription(description);
		repository.saveAndFlush(entity);
		attributes.addFlashAttribute("hasMessage", true);
		attributes.addFlashAttribute("class", "alert-info");
		attributes.addFlashAttribute("message", "区画の編集が完了しました。");
		return "redirect:/sections/detail/" + form.getId();
	}

	// 区画削除
	@GetMapping(path = "/sections/delete/{sectionId}")
	public String delete(@PathVariable Long sectionId, Model model, RedirectAttributes redirAttrs) throws IOException {
		repository.deleteById(sectionId);
		redirAttrs.addFlashAttribute("hasMessage", true);
		redirAttrs.addFlashAttribute("class", "alert-info");
		redirAttrs.addFlashAttribute("message", "区画の削除に成功しました。");
		return "redirect:/sections/list";
	}

	// 区画一覧表示
	@GetMapping(path = "/sections/list")
	public String showList(Principal principal, Model model) throws IOException {
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		List<Section> list = repository.findAllByUserIdOrderByUpdatedAtAsc(user.getUserId());
		model.addAttribute("list", list);
		return "/sections/list";
	}

	// 区画検索
	@PostMapping(path = "/sections/search")
	public String searchCrops(Principal principal, @RequestParam(name = "keyword", required = false) String keyword,
			Model model) {
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		List<Section> list = new ArrayList<>();
		if (keyword == null) {
			list = repository.findAllByUserIdOrderByUpdatedAtAsc(user.getUserId());
		} else if (keyword != null) {
			list = repository.findAllByNameContainingAndUserId(keyword, user.getUserId());
		}
		if (list.isEmpty()) {
			model.addAttribute("message", "その区画は見つかりませんでした。");
		}
		model.addAttribute("list", list);
		model.addAttribute("keyword", keyword);
		return "/sections/list";

	}

	// SectionエンティティからSectionFormエンティティの取得
	public SectionForm getSectionForm(Section section) throws IOException {
		modelMapper.getConfiguration().setAmbiguityIgnored(true);
		SectionForm form = modelMapper.map(section, SectionForm.class);
		return form;
	}
}