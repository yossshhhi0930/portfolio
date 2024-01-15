package com.example.portfolio.converter;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class DateToStringConverter implements Converter<Date, String> {

	@Override
	public String convert(Date source) {
		if (source == null) {
			return null; // もしsourceがnullの場合、nullを返す
		}

		// Date型を"MM-dd"形式の文字列に変換
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd");
		return dateFormat.format(source);
	}
}