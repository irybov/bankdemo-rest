package com.github.irybov.bankdemoboot.config;

import javax.sql.DataSource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@ComponentScan(basePackages="bankdemoboot")
public class AppConfig {

	@ConfigurationProperties(prefix = "spring.datasource")
	@Bean
	@Primary
	public DataSource datasource() {			
		return DataSourceBuilder.create().build();
	}
	
}
