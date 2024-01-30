package com.example.portfolio.converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * 文字列からDate型への変換を行うカスタムコンバータークラス
 */
@Component
public class DateConverter implements Converter<String, Date> {

	/**
	 * 文字列をDate型に変換する
	 *
	 * @param source 変換対象の文字列
	 * @return 変換されたDate型オブジェクト
	 * @throws IllegalArgumentException 不正な日付フォーマットの場合
	 */
	@Override
	public Date convert(String source) {
		if (source == null || source.isEmpty()) {
			return null; // もしsourceがnullまたは空文字の場合、nullを返す
		}

		// "MM-dd"形式の文字列をDate型に変換
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd");
		try {
			return dateFormat.parse(source);
		} catch (ParseException e) {
			// 不正な日付フォーマットの場合は例外をスロー
			throw new IllegalArgumentException("Invalid date format", e);
		}
	}
}
