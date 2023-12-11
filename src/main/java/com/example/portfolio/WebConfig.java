package com.example.portfolio;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.example.portfolio.converter.MonthDayConverter;
import com.example.portfolio.converter.MonthDayToStringConverter;
import com.example.portfolio.converter.LocalDateToStringConverter;
import com.example.portfolio.converter.LocalDateConverter;


@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Override
	public void addFormatters(FormatterRegistry registry) {
		registry.addConverter(new MonthDayConverter());
		registry.addConverter(new MonthDayToStringConverter());
		registry.addConverter(new LocalDateToStringConverter());
		registry.addConverter(new LocalDateConverter());

	}

}