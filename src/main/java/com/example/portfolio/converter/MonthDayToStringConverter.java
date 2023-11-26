package com.example.portfolio.converter;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class MonthDayToStringConverter implements Converter<MonthDay, String> {

    @Override
    public String convert(MonthDay monthDay) {
        // MonthDay型を"MM/dd"形式の文字列に変換
        return monthDay.format(DateTimeFormatter.ofPattern("MM/dd"));
    }
}