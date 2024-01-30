package com.example.portfolio.validation.constraints;

import java.util.Objects;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

/**
 * パスワードと確認用パスワードが一致するかを検証するカスタムバリデータの実装クラス
 */
public class PasswordEqualsValidator implements ConstraintValidator<PasswordEquals, Object> {

	private String field1;
	private String field2;
	private String message;

	/**
	 * アノテーションのパラメータの取得
	 */
	@Override
	public void initialize(PasswordEquals annotation) {
		field1 = "password";
		field2 = "passwordConfirmation";
		message = annotation.message();
	}

	/**
	 * パスワードと確認用パスワードが一致するかの検証
	 *
	 * @param value   バリデーション対象のオブジェクト
	 * @param context バリデーションコンテキスト
	 * @return パスワードと確認用パスワードが一致する場合はtrue、それ以外はfalse
	 */
	@Override
	public boolean isValid(Object value, ConstraintValidatorContext context) {
		// バリデーション対象オブジェクトのプロパティを取得
		BeanWrapper beanWrapper = new BeanWrapperImpl(value);
		String field1Value = (String) beanWrapper.getPropertyValue(field1);
		String field2Value = (String) beanWrapper.getPropertyValue(field2);
		// パスワードと確認用パスワードがどちらも空または一致する場合はtrue
		if ((field1Value.isEmpty() || field2Value.isEmpty()) || Objects.equals(field1Value, field2Value)) {
			return true;
		} else {
			// 一致しない場合はカスタムエラーメッセージを設定し、バリデーションエラーとして処理
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(message).addPropertyNode(field2).addConstraintViolation();
			return false;
		}
	}

}
