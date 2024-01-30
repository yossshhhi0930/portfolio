package com.example.portfolio;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * アプリケーションの設定に関するクラス
 */
@Configuration
public class AppConfig {

	/**
	 * ModelMapperのBeanを作成して返す
	 *
	 * @return ModelMapperのBeanインスタンス
	 */
	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}

}
