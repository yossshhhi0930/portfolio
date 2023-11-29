package com.example.portfolio.controller;

import java.io.IOException;
import java.security.Principal;

import javax.validation.Valid;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.portfolio.entity.Crop;
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
	
//区画データ新規登録formの表示
	@GetMapping(path = "/sections/new")
	public String newSection(Model model) {
		SectionForm section = new SectionForm();
		model.addAttribute("form", section);
		return "sections/new";

	}
	
	// formから送られた区画データを登録
		@RequestMapping(value = "/section", method = RequestMethod.POST)
		public String create(Principal principal, @Validated @ModelAttribute("form") SectionForm form,
				BindingResult result, Model model, RedirectAttributes attributes)
				throws IOException {

			// 同一の作物が既に登録されている場合、エラー項目に追加
			if (repository.findByName(form.getName()) != null) {
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

			//SectionEntityのインスタンスを生成
			Section entity = new Section();
			Authentication authentication = (Authentication) principal;
			UserInf user = (UserInf) authentication.getPrincipal();
			entity.setUserId(user.getUserId());
			entity.setName(form.getName());
			entity.setDescription(form.getDescription());

			// entityの保存
			repository.saveAndFlush(entity);
			// 画像を表示するたmodelめのimageListを作成
			attributes.addFlashAttribute("hasMessage", true);
			attributes.addFlashAttribute("class", "alert-info");
			attributes.addFlashAttribute("message", "作物データ登録に成功しました。");
			return "redirect:sections/detail/" + entity.getId();

		}

}