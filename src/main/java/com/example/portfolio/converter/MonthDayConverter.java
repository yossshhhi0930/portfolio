package com.example.portfolio.converter;

import java.time.MonthDay;
import java.time.format.DateTimeFormatter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class MonthDayConverter implements Converter<String, MonthDay> {

    @Override
    public MonthDay convert(String source) {
    	if (source == null || source.isEmpty()) {
            return null; // もしsourceがnullまたは空文字の場合、nullを返す
        }
        // "MM/dd"形式の文字列をMonthDay型に変換
        return MonthDay.parse(source, DateTimeFormatter.ofPattern("MM/dd"));
    }
}