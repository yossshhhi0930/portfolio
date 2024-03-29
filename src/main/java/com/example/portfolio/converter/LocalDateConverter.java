package com.example.portfolio.converter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * 文字列からLocalDate型への変換を行うカスタムコンバータークラス
 */
@Component
public class LocalDateConverter implements Converter<String, LocalDate> {

	/**
	 * 文字列をLocalDate型に変換する
	 *
	 * @param source 変換対象の文字列
	 * @return 変換されたLocalDate型オブジェクト
	 */
	@Override
	public LocalDate convert(String source) {
		if (source == null || source.isEmpty()) {
			return null; // もしsourceがnullまたは空文字の場合、nullを返す
		}
		// "yyyy-MM-dd"形式の文字列をLocalDate型に変換
		return LocalDate.parse(source, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
	}
}