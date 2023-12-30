package com.example.portfolio.converter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class LocalDateToStringConverter implements Converter<LocalDate, String> {

	@Override
	public String convert(LocalDate localDate) {
		if (localDate == null) {
			return null; // もしsourceがnullまたは空文字の場合、nullを返す
		}
		// MonthDay型を"MM/dd"形式の文字列に変換
		return localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
	}
}