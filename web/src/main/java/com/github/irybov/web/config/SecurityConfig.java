package com.github.irybov.web.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

//import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.InMemoryUserDetailsManagerConfigurer;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
//import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.github.irybov.service.security.AccountDetailsService;
import com.github.irybov.web.security.JWTFilter;
import com.github.irybov.web.security.OTPFilter;

@EnableWebSecurity
//@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

	@Value("${server.address}")
	private String uri;
	@Value("${server.port}")
	private int port;
//	@Value("${management.server.port}")
//	private int m_port;
	@Value("${external.payment-service}")
	private String externalURL;

	@Autowired
	private JWTFilter jwtFilter;
//	@Autowired
//	private OTPFilter otpFilter;
//	@Autowired
//	private DataSource dataSource;
	private final PasswordEncoder passwordEncoder;
	private final UserDetailsService accountDetailsService;
	public SecurityConfig(PasswordEncoder passwordEncoder, UserDetailsService accountDetailsService) {
		this.passwordEncoder = passwordEncoder;
		this.accountDetailsService = accountDetailsService;
	}
	
    private static final String[] WHITE_LIST_URLS = {
//    		"/home", 
    		"/login", 
    		"/register", 
//    		"/confirm", 
    		"/activate/*", 
//    		"/webjars/**", 
//    		"/css/**", 
//    		"/js/**", 
			"/bills/external"
    };
    private static final String[] SHARED_LIST_URLS = {
    		"/token", 
    		"/bills/*", 
    		"/accounts/{phone}"
    };
    private static final String[] ADMINS_LIST_URLS = {
			"/control", 
    		"/accounts/{phone}/search", 
    		"/accounts/{id}/status", 
    		"/accounts", 
    		"/bills/{id}/status", 
			"/operations/{id}/*", 
			"/h2-console/**"
    };
//    private static final String[] SWAGGER_LIST_URLS = {
////    		"/configuration/**", 
//			"/dox/swagger-ui/**", 
//			"/dox/v*/api-docs/**"
////    		"/webjars/**"
//    };
//    private static final String[] REMOTE_LIST_URLS = {
//			"**/swagger*/**", 
//			"/**/api-docs/**", 
//    		"/actuator/**"
//    };
	
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
	
//	@Bean
//	public AuthenticationManager authManager(HttpSecurity http) throws Exception {
	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		
//		AuthenticationManagerBuilder auth = http.getSharedObject(AuthenticationManagerBuilder.class);
		
		DaoAuthenticationProvider dao = new DaoAuthenticationProvider();
		dao.setUserDetailsService(accountDetailsService);
		dao.setPasswordEncoder(passwordEncoder);
		
        UserDetails remote = User.builder()
                .username("remote")
                .password(passwordEncoder.encode("remote"))
                .roles("REMOTE")
                .build();
        
//		auth.inMemoryAuthentication()
        inMemoryConfigurer()
			.withUser(remote)
			.passwordEncoder(passwordEncoder)
			.configure(auth);
        
		auth.authenticationProvider(dao);
			 
//	    return auth.build();
	}
	private InMemoryUserDetailsManagerConfigurer<AuthenticationManagerBuilder> inMemoryConfigurer() {
		return new InMemoryUserDetailsManagerConfigurer<>();
	}
	
    @Bean
    public AuthenticationManager authenticationManager(
    		AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
    
	@Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().antMatchers("/error");
    }
	
	@Bean
	@Order(1)
	public SecurityFilterChain actuatorChain(HttpSecurity http) throws Exception {
		
		http
			.csrf().disable()
			.sessionManagement()
	        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				.and()
			.antMatcher("/actuator/**")
			.authorizeRequests()
			.antMatchers("/actuator/**").hasRole("REMOTE")
			.anyRequest().authenticated()
				.and()
			.httpBasic();
        
		return http.build();
	}

	@Bean
	@Order(2)
	public SecurityFilterChain swaggerChain(HttpSecurity http) throws Exception {
		
		http
			.csrf().disable()
			.sessionManagement()
	        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				.and()
			.antMatcher("/dox/**")
			.authorizeRequests()
			.antMatchers("/dox/**").hasRole("ADMIN")
			.anyRequest().authenticated()
				.and()
			.httpBasic();
        
		return http.build();
	}
	
    @Bean
    @Order(3)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    	        
//    	CsrfTokenRepository csrfTokenRepository = new HttpSessionCsrfTokenRepository();   	
/*        UserDetails remote = User.builder()
			                .username("remote")
			                .password(passwordEncoder.encode("remote"))
			                .roles("REMOTE")
			                .build();
    	
    	AuthenticationManagerBuilder auth = http.getSharedObject(AuthenticationManagerBuilder.class);
		auth.inMemoryAuthentication()
//			.withUser("remote")
			.withUser(remote)
			.passwordEncoder(passwordEncoder);
//			.password(passwordEncoder.encode("remote"))
//			.roles("REMOTE");		
        
	    DaoAuthenticationProvider dao = new DaoAuthenticationProvider();
	    dao.setUserDetailsService(accountDetailsService);
	    dao.setPasswordEncoder(passwordEncoder);
	    
	    auth.authenticationProvider(dao);*/
//	    auth.build();

		http
//			.cors()
//			.cors(Customizer.withDefaults())
//			.cors().configurationSource(corsConfigurationSource())
//				.and()
			.csrf().disable()
//			.authenticationProvider(dao)
//			.authenticationManager(authManager)
//			.sessionManagement().disable()
			.sessionManagement()
	        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				.and()
			.authorizeRequests()
			.mvcMatchers(WHITE_LIST_URLS).permitAll()
			.mvcMatchers(SHARED_LIST_URLS).hasAnyRole("ADMIN", "CLIENT")
			.mvcMatchers(ADMINS_LIST_URLS).hasRole("ADMIN")
//			.antMatchers("/actuator/**").hasRole("REMOTE")
			.anyRequest().authenticated()
				.and()
			.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
			.addFilterAfter(otpFilter(), UsernamePasswordAuthenticationFilter.class);
//			.httpBasic();
/*		    .csrf().csrfTokenRepository(csrfTokenRepository)
		    .sessionAuthenticationStrategy(new CsrfAuthenticationStrategy(csrfTokenRepository))
		    .ignoringAntMatchers("/bills/external", "/actuator/**")*/
//		    					 "/webjars/**", 
//		    					 "/configuration/**", 
//		    					 "/swagger*/**", 
//		    					 "/**/api-docs/**")
//		    .ignoringAntMatchers(REMOTE_LIST_URLS)
//		        .and()
/*			.formLogin()
			.usernameParameter("phone")
			.loginPage("/home")
			.loginProcessingUrl("/auth")
//			.successHandler((request, response, authentication) ->
//				response.sendRedirect("/accounts/show/" + authentication.getName()))
			.defaultSuccessUrl("/success", true)
            .failureUrl("/login?error=true")
				.and()*/
/*			.logout()
//          .logoutUrl("/logout")
            .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST"))
            .invalidateHttpSession(true)
            .clearAuthentication(true)
            .deleteCookies("JSESSIONID")
			.logoutSuccessUrl("/home")
				.and()*/
//		http.headers().frameOptions().disable();

        return http.build();
    }
    
    @Bean
    public OTPFilter otpFilter() {
    	return new OTPFilter();
    }
	
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
    public CorsConfigurationSource corsConfigurationSource() {

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList("http://" + uri + ":" + port));
//		configuration.setAllowedOriginPatterns(Arrays.asList("http://" + uri +":" + port));
		configuration.setAllowedMethods(Arrays.asList("*"));
		configuration.setAllowCredentials(true);
		configuration.setExposedHeaders(Arrays.asList("*"));
//		configuration.setMaxAge(1800L);
		configuration.setAllowedHeaders(Arrays.asList("*"));
		source.registerCorsConfiguration("/**", configuration);
		return source;
    }
/*    
    @Bean
    public CorsFilter corsFilter() {
        return new CorsFilter(corsConfigurationSource());
    }
*/
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
            	
        		registry.addMapping("/bills/external")
				.allowedOrigins(externalURL)
//				.allowedOriginPatterns("http://localhost:[4567]")
				.allowedMethods(RequestMethod.OPTIONS.name(), RequestMethod.POST.name())
				.allowedHeaders("*")
				.exposedHeaders("*")
//				.maxAge(1800L)
				.allowCredentials(false);

        		registry.addMapping("/activate/*")
				.allowedOrigins("*")
				.allowedMethods(RequestMethod.GET.name())
				.allowedHeaders("*")
				.exposedHeaders("*")
//				.maxAge(1800L)
				.allowCredentials(false);
        		
        		registry.addMapping("/**")
				.allowedOriginPatterns("http://" + uri + ":" + "[*]")
				.allowedMethods("*")
				.allowedHeaders("*")
				.exposedHeaders("*")
//				.maxAge(1800L)
				.allowCredentials(true);
            }
        };
    }

}
