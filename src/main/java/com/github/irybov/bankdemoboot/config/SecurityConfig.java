package com.github.irybov.bankdemoboot.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;

//import javax.sql.DataSource;

//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//import org.springframework.web.servlet.config.annotation.CorsRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.github.irybov.bankdemoboot.security.AccountDetailsService;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter{

	@Value("${server.address}")
	private String uri;
	@Value("${server.port}")
	private int port;
	
//	@Autowired
//	private DataSource dataSource;
	
	@Bean
	protected BCryptPasswordEncoder passwordEncoder() {
	    return new BCryptPasswordEncoder(4);
	}
	
    private final AccountDetailsService accountDetailsService;
    public SecurityConfig(AccountDetailsService accountDetailsService) {
        this.accountDetailsService = accountDetailsService;
    }	
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(accountDetailsService)
            .passwordEncoder(passwordEncoder());
    }
	
/*	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		
		auth.jdbcAuthentication().dataSource(dataSource)
			.usersByUsernameQuery("SELECT phone, password, is_active::int "
								+ "FROM bankdemo.accounts WHERE phone=?")
		    .authoritiesByUsernameQuery
		    ("SELECT phone, roles FROM bankdemo.accounts AS a INNER JOIN bankdemo.roles AS r "
		    + "ON a.id=r.account_id WHERE a.phone=?")
		    .passwordEncoder(passwordEncoder())
		    .rolePrefix("ROLE_");
	}*/
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {

		http
//			.cors(Customizer.withDefaults())
//			.cors()
//			.and()
			.authorizeRequests()
			.antMatchers("/login", "/register", "/confirm", "/webjars/**", "/css/**", "/js/**",
					"/bills/external", "/**/swagger*/**", "/**/api-docs/**")
			.permitAll()
				.and()
			.authorizeRequests()
			.antMatchers("/bills/**", "/accounts/show", "/accounts/password")
			.hasAnyRole("ADMIN", "CLIENT")
			.antMatchers("/accounts/search", "/accounts/status", "/accounts/list/**", "/actuator/**",
				"/control", "/h2-console/**", "/operations/**")
			.hasRole("ADMIN")
			.anyRequest().authenticated()
				.and()
		    .csrf()
		    .ignoringAntMatchers("/control", "/bills/external")
		        .and()
			.formLogin()
			.usernameParameter("phone")
			.loginPage("/home")
			.loginProcessingUrl("/auth")
//			.successHandler((request, response, authentication) ->
//			response.sendRedirect("/accounts/show/" + authentication.getName()))
			.defaultSuccessUrl("/success", true)
            .failureUrl("/login?error=true")
            .permitAll()
				.and()
			.logout()
//          .logoutUrl("/logout")
            .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST"))
            .invalidateHttpSession(true)
            .clearAuthentication(true)
            .deleteCookies("JSESSIONID")
			.logoutSuccessUrl("/home")
			.permitAll();
//			.and().cors().configurationSource(corsConfigurationSource());
//		http.headers().frameOptions().disable();
	}
	
/*    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://" + uri + ":" + port)
                .allowedMethods("*");
    }*/
	
    @Bean
    CorsConfigurationSource corsConfigurationSource() {

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList("http://" + uri +":" + port));
		configuration.setAllowedMethods(Arrays.asList("GET", "OPTIONS", "PATCH"));
		configuration.setAllowCredentials(true);
		configuration.setExposedHeaders(Arrays.asList("*"));
//		configuration.setMaxAge(3600L);
		configuration.setAllowedHeaders(Arrays.asList("*"));
		source.registerCorsConfiguration("/**", configuration);
		return source;
    }
    
}
