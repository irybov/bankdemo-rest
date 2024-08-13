package com.github.irybov.bankdemorest.config;

import java.util.concurrent.TimeUnit;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetails;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.irybov.bankdemorest.controller.dto.LoginRequest;

@EnableCaching
@Configuration
public class CaffeineConfig {
	
	@Bean
	public Cache<String, LoginRequest> cacheConfig() {
	    return Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build();
	}
/*	
	@Bean
	public Caffeine<Object, Object> caffeineConfig() {
	    return Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES);
	}

	@Bean
	public CacheManager cacheManager(Caffeine<Object, Object> caffeine) {
	    CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
	    caffeineCacheManager.setCaffeine(caffeine);
	    return caffeineCacheManager;
	}
*/	
}
