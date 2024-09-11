package com.github.irybov.database.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan("com.github.irybov.database")
@EntityScan("com.github.irybov.database")
@EnableJpaRepositories(enableDefaultTransactions = false, basePackages = "com.github.irybov.database")
public class ModuleConfig {}
