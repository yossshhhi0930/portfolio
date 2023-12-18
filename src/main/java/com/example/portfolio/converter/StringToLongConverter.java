//package com.example.portfolio.converter;
//
//import java.time.LocalDate;
//import java.time.MonthDay;
//import java.time.format.DateTimeFormatter;
//
//import org.springframework.core.convert.converter.Converter;
//import org.springframework.stereotype.Component;
//
//@Component
//public class StringToLongConverter implements Converter<String, Long> {
//
//    @Override
//    public Long convert(String source) {
//    	if (source == null || source.isEmpty()) {
//            return null; // もしsourceがnullまたは空文字の場合、nullを返す
//        }
//    	return Long.parseLong(source);
//    }
//}