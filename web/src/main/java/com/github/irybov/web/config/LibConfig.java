package com.github.irybov.web.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = {com.github.irybov.service.config.ServiceConfig.class, 
									 com.github.irybov.database.config.ModuleConfig.class})
public class LibConfig {}
