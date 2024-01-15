package com.example.portfolio;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.example.portfolio.converter.DateConverter;
import com.example.portfolio.converter.DateToStringConverter;
import com.example.portfolio.converter.LocalDateToStringConverter;
import com.example.portfolio.converter.LocalDateConverter;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Override
	public void addFormatters(FormatterRegistry registry) {
		registry.addConverter(new DateConverter());
		registry.addConverter(new DateToStringConverter());
		registry.addConverter(new LocalDateToStringConverter());
		registry.addConverter(new LocalDateConverter());
	}

}