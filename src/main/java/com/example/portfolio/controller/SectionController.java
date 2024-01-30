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

/**
 * 区画に関する操作（登録、編集、表示、削除）を担当するコントローラークラス
 */
@Controller
public class SectionController {

	// ロガーの初期化
	protected static Logger log = LoggerFactory.getLogger(CropController.class);

	// 区画リポジトリの注入
	@Autowired
	SectionRepository repository;

	// ModelMapperの注入
	@Autowired
	private ModelMapper modelMapper;

	/**
	 * 区画登録画の表示
	 *
	 * @param model ビューで使用するモデル
	 * @return 区画登録画面
	 */
	@GetMapping(path = "/sections/new")
	public String newSection(Model model) {
		// 区画フォームを初期化
		SectionForm form = new SectionForm();
		// 初期化した区画フォームをモデルに設定
		model.addAttribute("form", form);
		// 区画登録画面を返す
		return "sections/new";
	}

	/**
	 * 区画の登録<br>
	 * 区画登録画面から送信されたフォームデータの検証、保存を行う
	 *
	 * @param principal  認証情報
	 * @param form       区画登録フォームデータ
	 * @param result     フォーム検証の結果
	 * @param model      ビューで使用するモデル
	 * @param attributes リダイレクト属性
	 * @return 検証結果に基づく画面
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	@PostMapping(path = "/section")
	public String create(Principal principal, @Validated @ModelAttribute("form") SectionForm form, BindingResult result,
			Model model, RedirectAttributes attributes) throws IOException {
		// 認証情報からユーザーを取得
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		// フォームデータを変数に代入
		Long userId = user.getUserId();
		String name = form.getName();
		String description = form.getDescription();
		// 同一の区画が既に登録されている場合に返すエラーの追加
		if (repository.findByNameAndUserId(name, userId) != null) {
			FieldError fieldError = new FieldError(result.getObjectName(), "name", "その区画名は既に登録されています。");
			result.addError(fieldError);
		}
		// エラーがあった場合に、区画登録画面を返し、エラーメッセージを表示
		if (result.hasErrors()) {
			// アラートメッセージをモデルに設定
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-danger");
			model.addAttribute("message", "	区画の登録に失敗しました。");
			// 区画登録画面を返す
			return "sections/new";
		}

		// エラーが無かった場合の処理
		// フォームデータで、区画エンティティを生成
		Section entity = new Section(userId, name, description);
		// 区画エンティティを保存
		repository.saveAndFlush(entity);
		// アラートメッセージをリダイレクト先モデルに設定
		attributes.addFlashAttribute("hasMessage", true);
		attributes.addFlashAttribute("class", "alert-info");
		attributes.addFlashAttribute("message", "区画の登録が完了しました。");
		// リダイレクト先に登録した区画IDのパラメータを渡し、区画詳細画面を返す
		return "redirect:sections/detail/" + entity.getId();
	}

	/**
	 * 区画詳細画面の表示
	 *
	 * @param sectionId 表示する区画のID
	 * @param model     ビューで使用するモデル
	 * @return 区画詳細画面
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	@GetMapping(path = "/sections/detail/{sectionId}")
	public String showDetail(@PathVariable Long sectionId, Model model) throws IOException {
		// 区画IDから区画を取得
		Optional<Section> optionalSection = repository.findById(sectionId);
		Section entity = optionalSection.orElseThrow(() -> new RuntimeException("Section not found"));
		// 取得した区画情報をモデルに設定する
		model.addAttribute("section", entity);
		// 区画詳細画面を返す
		return "sections/detail";
	}

	/**
	 * 区画の編集画面を表示
	 *
	 * @param sectionId 編集する区画のID
	 * @param model     ビューで使用するモデル
	 * @return 区画編集画面
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	@GetMapping(path = "/sections/edit/{sectionId}")
	public String showEditPage(@PathVariable Long sectionId, Model model) throws IOException {
		// 区画IDから区画を取得
		Optional<Section> optionalSection = repository.findById(sectionId);
		Section entity = optionalSection.orElseThrow(() -> new RuntimeException("Section not found"));
		// 区画から区画フォームを生成
		SectionForm form = getSectionForm(entity);
		// 取得した区画フォームをモデルに設定
		model.addAttribute("form", form);
		// 区画編集画面を返す
		return "sections/edit";
	}

	/**
	 * 区画の編集<br>
	 * 区画編集画面から送信されたフォームデータの検証、保存を行う
	 *
	 * @param principal  認証情報
	 * @param form       区画フォームデータ
	 * @param result     フォーム検証の結果
	 * @param model      ビューで使用するモデル
	 * @param attributes リダイレクト属性
	 * @return 検証結果に基づく画面
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	@PostMapping(path = "/sections/edit-complete")
	public String edit(Principal principal, @Validated @ModelAttribute("form") SectionForm form, BindingResult result,
			Model model, RedirectAttributes attributes) throws IOException {
		// 認証情報からユーザーを取得
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		// フォームデータを変数に代入
		Long userId = user.getUserId();
		Long id = form.getId();
		String name = form.getName();
		String description = form.getDescription();
		// フォームデータの区画IDから区画を取得
		Optional<Section> optionalSection = repository.findById(id);
		Section entity = optionalSection.orElseThrow(() -> new RuntimeException("Section not found"));
		// 区画名を変更し、かつ同一の区画が既に登録されている場合に返すエラーの追加
		if (!entity.getName().equals(name) && repository.findByNameAndUserId(name, userId) != null) {
			FieldError fieldError = new FieldError(result.getObjectName(), "name", "その区画名は既に登録されています。");
			result.addError(fieldError);
		}
		// エラーがあった場合に、区画編集画面を返し、エラーメッセージを表示
		if (result.hasErrors()) {
			// アラートメッセージをモデルに設定
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-danger");
			model.addAttribute("message", "区画の編集に失敗しました。");
			// 区画編集画面を返す
			return "/sections/edit";
		}
		// フォームデータで区画エンティティを更新
		entity.setName(name);
		entity.setDescription(description);
		// 更新された区画エンティティを保存
		repository.saveAndFlush(entity);
		// アラートメッセージをリダイレクト先のモデルに設定
		attributes.addFlashAttribute("hasMessage", true);
		attributes.addFlashAttribute("class", "alert-info");
		attributes.addFlashAttribute("message", "区画の編集が完了しました。");
		// リダイレクト先に編集した区画IDのパラメータを渡し、区画詳細画面を返す
		return "redirect:/sections/detail/" + form.getId();
	}

	/**
	 * 区画の削除
	 *
	 * @param sectionId  削除する区画のID
	 * @param model      ビューで使用するモデル
	 * @param redirAttrs リダイレクト属性
	 * @return 区画一覧画面
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	@GetMapping(path = "/sections/delete/{sectionId}")
	public String delete(@PathVariable Long sectionId, Model model, RedirectAttributes redirAttrs) throws IOException {
		// 区画IDから区画を削除
		repository.deleteById(sectionId);
		// アラートメッセージをリダイレクト先のモデルに設定
		redirAttrs.addFlashAttribute("hasMessage", true);
		redirAttrs.addFlashAttribute("class", "alert-info");
		redirAttrs.addFlashAttribute("message", "区画の削除に成功しました。");
		// 区画一覧画面を返す
		return "redirect:/sections/list";
	}

	/**
	 * 区画一覧を表示
	 *
	 * @param principal 認証情報
	 * @param model     ビューで使用するモデル
	 * @return 区画一覧画面
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	@GetMapping(path = "/sections/list")
	public String showList(Principal principal, Model model) throws IOException {
		// 認証情報からユーザ情報を取得
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		// ユーザに対応する全ての区画を取得しリストを生成
		List<Section> list = repository.findAllByUserIdOrderByUpdatedAtAsc(user.getUserId());
		// 生成したリストをモデルに設定
		model.addAttribute("list", list);
		// 区画一覧画面を返す
		return "/sections/list";
	}

	/**
	 * 区画の検索<br>
	 * 区画一覧画面から作物検索を行う（キーワードでの絞り込み検索機能あり）
	 *
	 * @param principal 認証情報
	 * @param keyword   検索キーワード
	 * @param model     ビューで使用するモデル
	 * @return 区画一覧画面
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	@PostMapping(path = "/sections/search")
	public String searchCrops(Principal principal, @RequestParam(name = "keyword", required = false) String keyword,
			Model model) throws IOException {
		// 認証情報からユーザ情報を取得
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		// 検索結果として返す区画リストを初期化
		List<Section> list = new ArrayList<>();
		// キーワードが指定されていない場合はユーザに紐づく全ての区画を取得し、区画リストに代入
		if (keyword == null) {
			list = repository.findAllByUserIdOrderByUpdatedAtAsc(user.getUserId());
		} else if (keyword != null) {
			// キーワードが指定されている場合は、区画名にキーワードを含む区画を取得し、区画リストに代入
			list = repository.findAllByNameContainingAndUserId(keyword, user.getUserId());
		}
		// 検索結果として返す区画リストが空の場合、モデルにメッセージを設定
		if (list.isEmpty()) {
			model.addAttribute("message", "その区画は見つかりませんでした。");
		}
		// 区画リストをモデルに設定
		model.addAttribute("list", list);
		// パラメータとして送られてきた検索キーワードを、モデルに設定
		model.addAttribute("keyword", keyword);
		// 区画一覧画面を返す
		return "/sections/list";

	}

	/**
	 * 区画エンティティから区画フォームオブジェクトを生成するメソッド
	 *
	 * @param section 区画エンティティ
	 * @return 区画フォームエンティティ
	 * @throws IOException プロセス中にIO例外が発生した場合
	 */
	public SectionForm getSectionForm(Section section) throws IOException {
		// ModelMapperの設定: 重複があっても無視する
		modelMapper.getConfiguration().setAmbiguityIgnored(true);
		// 区画エンティティから区画フォームオブジェクトへの変換
		SectionForm form = modelMapper.map(section, SectionForm.class);
		return form;
	}
}