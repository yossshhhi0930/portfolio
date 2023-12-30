package com.example.portfolio.converter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class LocalDateConverter implements Converter<String, LocalDate> {

	@Override
	public LocalDate convert(String source) {
		if (source == null || source.isEmpty()) {
			return null; // もしsourceがnullまたは空文字の場合、nullを返す
		}
		// "MM/dd"形式の文字列をMonthDay型に変換
		return LocalDate.parse(source, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
	}
}