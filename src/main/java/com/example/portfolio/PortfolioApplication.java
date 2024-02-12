package com.example.portfolio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * アプリケーションの起動クラス
 */
@SpringBootApplication
@EnableJpaRepositories("com.example.portfolio.repository")
@ComponentScan(basePackages = "com.example.portfolio")
@EnableScheduling
public class PortfolioApplication extends SpringBootServletInitializer {

	/**
	 * アプリケーションのエントリーポイントです。
	 *
	 * @param args コマンドライン引数
	 */
	public static void main(String[] args) {
		SpringApplication.run(PortfolioApplication.class, args);
	}

	/**
	 * SpringBootServletInitializerを拡張して、WARファイルとしてデプロイするための設定を提供します。
	 *
	 * @param application SpringApplicationBuilderインスタンス
	 * @return 拡張されたSpringApplicationBuilderインスタンス
	 */
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(PortfolioApplication.class);
	}
}
