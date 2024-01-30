package com.example.portfolio.converter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * LocalDate型から文字列への変換を行うカスタムコンバータークラス
 */
@Component
public class LocalDateToStringConverter implements Converter<LocalDate, String> {

	/**
	 * LocalDate型を文字列に変換する
	 *
	 * @param source 変換対象のLocalDate型オブジェクト
	 * @return 変換された文字列（"MM-dd"形式）
	 */
	@Override
	public String convert(LocalDate localDate) {
		if (localDate == null) {
			return null; // もしsourceがnullまたは空文字の場合、nullを返す
		}
		// LocalDate型を"yyyy-MM-dd"形式の文字列に変換
		return localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
	}
}