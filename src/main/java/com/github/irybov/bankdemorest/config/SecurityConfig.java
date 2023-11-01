package com.github.irybov.bankdemorest.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;

//import javax.sql.DataSource;

//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
//import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
//import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//import org.springframework.web.servlet.config.annotation.CorsRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.github.irybov.bankdemorest.security.AccountDetailsService;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
		
    private final AccountDetailsService accountDetailsService;
    public SecurityConfig(AccountDetailsService accountDetailsService) {
        this.accountDetailsService = accountDetailsService;
    }

	@Value("${server.address}")
	private String uri;
	@Value("${server.port}")
	private int port;
//	@Value("${management.server.port}")
//	private int m_port;
	
//	@Autowired
//	private DataSource dataSource;
	
    private static final String[] WHITE_LIST_URLS = { 
    		"/home", 
    		"/login", 
    		"/register", 
    		"/confirm", 
			"/bills/external", 
    		"/css/**", 
    		"/js/**", 
    		"/webjars/**"
    };
    private static final String[] SHARED_LIST_URLS = {
    		"/bills/**", 
    		"/accounts/show", 
    		"/accounts/password"
    };
    private static final String[] ADMIN_LIST_URLS = {
			"/**/swagger*/**", 
			"/**/api-docs/**", 
			"/control", 
    		"/accounts/search", 
    		"/accounts/status", 
    		"/accounts/list/**", 
			"/operations/**", 
			"/h2-console/**"
    };
//    private static final String[] REMOTE_LIST_URLS = {
//    		"/actuator/**"
//    };
	
	@Bean
	protected BCryptPasswordEncoder passwordEncoder() {
	    return new BCryptPasswordEncoder(4);
	}
/*	@Bean
	public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
		
        AuthenticationManagerBuilder auth = http.getSharedObject(AuthenticationManagerBuilder.class);
		auth.inMemoryAuthentication()
			.withUser("remote")
			.password(passwordEncoder().encode("remote"))
			.roles("REMOTE");
        
	    DaoAuthenticationProvider dao = new DaoAuthenticationProvider();
	    dao.setUserDetailsService(accountDetailsService);
	    dao.setPasswordEncoder(passwordEncoder());
	    
	    auth.authenticationProvider(dao);        
	    return auth.build();
	}*/	
	
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
	
/*	@Bean
	public AuthenticationProvider authenticationProvider() {
		
	    DaoAuthenticationProvider dao = new DaoAuthenticationProvider();
	    dao.setUserDetailsService(accountDetailsService);
	    dao.setPasswordEncoder(passwordEncoder());
	 
	    return dao;
	}*/
	
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    	        
//   		CsrfTokenRepository csrfTokenRepository = new HttpSessionCsrfTokenRepository();
    	
    	AuthenticationManagerBuilder auth = http.getSharedObject(AuthenticationManagerBuilder.class);
		auth.inMemoryAuthentication()
			.withUser("remote")
			.password(passwordEncoder().encode("remote"))
			.roles("REMOTE");		
        
	    DaoAuthenticationProvider dao = new DaoAuthenticationProvider();
	    dao.setUserDetailsService(accountDetailsService);
	    dao.setPasswordEncoder(passwordEncoder());
	    
	    auth.authenticationProvider(dao);
//	    auth.build();

		http
			.authenticationProvider(dao)
			.authenticationManager(auth.build())
//			.sessionManagement()
//	        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//			.cors(Customizer.withDefaults())
//			.cors()
//			.and()
			.authorizeRequests()
			.antMatchers(WHITE_LIST_URLS).permitAll()
			.antMatchers(SHARED_LIST_URLS).hasAnyRole("ADMIN", "CLIENT")
			.antMatchers(ADMIN_LIST_URLS).hasRole("ADMIN")
			.antMatchers("/actuator/**").hasRole("REMOTE")
			.anyRequest().authenticated()
				.and()
		    .csrf().disable()
/*		    .csrfTokenRepository(csrfTokenRepository)
		    .sessionAuthenticationStrategy(new CsrfAuthenticationStrategy(csrfTokenRepository))
		    .ignoringAntMatchers("/bills/external", "/actuator/**")*/
//		    .ignoringAntMatchers(REMOTE_LIST_URLS)
//		        .and()
			.formLogin()
			.usernameParameter("phone")
			.loginPage("/home")
			.loginProcessingUrl("/auth")
//			.successHandler((request, response, authentication) ->
//			response.sendRedirect("/accounts/show/" + authentication.getName()))
			.defaultSuccessUrl("/success", true)
            .failureUrl("/login?error=true")
				.and()
			.logout()
//          .logoutUrl("/logout")
            .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST"))
            .invalidateHttpSession(true)
            .clearAuthentication(true)
            .deleteCookies("JSESSIONID")
			.logoutSuccessUrl("/home")
				.and()
			.httpBasic();
//			.and().cors().configurationSource(corsConfigurationSource());
//		http.headers().frameOptions().disable();

        return http.build();
    }
    
/*	@Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().antMatchers("/css/**", "/js/**", "/webjars/**");
    }*/
	
//    @Override
//    public void configure(WebSecurity web) throws Exception {
//        web.ignoring().antMatchers("/**/swagger*/**", "/**/api-docs/**");
//    }
    
//    @Configuration
//    @Order(Ordered.HIGHEST_PRECEDENCE)
//    public static class RemoteSecurityConfig extends WebSecurityConfigurerAdapter {
//    	
//    	@Override
//        protected void configure(HttpSecurity http) throws Exception {
//    		
//            http
//            	.csrf().disable()
//                .antMatcher("/**/swagger*/**")
//                .authorizeRequests()
//                .anyRequest().hasRole("REMOTE")
//        			.and()
//                .httpBasic();
//        }
//    	
//    }
	
/*    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://" + uri + ":" + port)
                .allowedMethods("*");
    }*/
	
//    @Bean
    CorsConfigurationSource corsConfigurationSource() {

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList("http://" + uri +":" + port));
//		configuration.setAllowedOriginPatterns(Arrays.asList("http://" + uri +":" + port));
		configuration.setAllowedMethods(Arrays.asList("*"));
		configuration.setAllowCredentials(true);
		configuration.setExposedHeaders(Arrays.asList("*"));
//		configuration.setMaxAge(1800L);
		configuration.setAllowedHeaders(Arrays.asList("*"));
		source.registerCorsConfiguration("/**", configuration);
		return source;
    }
    
}
