//package com.example.portfolio.converter;
//import java.time.MonthDay;
//import java.time.format.DateTimeFormatter;
//
//import org.springframework.core.convert.converter.Converter;
//import org.springframework.stereotype.Component;
//
//@Component
//public class LongToStringConverter implements Converter<Long, String> {
//
//    @Override
//    public String convert(Long source) {
//    	if (source == null) {
//            return null; // もしsourceがnullまたは空文字の場合、nullを返す
//        }
//    	return String.valueOf(source);
//    }
//}