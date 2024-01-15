package com.example.portfolio.converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class DateConverter implements Converter<String, Date> {

	 @Override
	    public Date convert(String source) {
	        if (source == null || source.isEmpty()) {
	            return null; // もしsourceがnullまたは空文字の場合、nullを返す
	        }

	        // "MM/dd"形式の文字列をDate型に変換
	        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd");
	        try {
	            return dateFormat.parse(source);
	        } catch (ParseException e) {
	            throw new IllegalArgumentException("Invalid date format", e);
	        }
	    }
	}






