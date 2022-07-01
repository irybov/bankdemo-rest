package com.github.irybov.bankdemoboot.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter{

	@Autowired
	private DataSource dataSource;
	
	@Bean
	protected BCryptPasswordEncoder passwordEncoder() {
	    return new BCryptPasswordEncoder(4);
	}
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {		
		auth.jdbcAuthentication().dataSource(dataSource)
			.usersByUsernameQuery("SELECT phone, password, active::int FROM bankdemo.accounts WHERE phone=?")
		    .authoritiesByUsernameQuery
		    ("SELECT phone, roles FROM bankdemo.accounts AS a INNER JOIN bankdemo.roles AS r"
		    + " ON a.id=r.account_id WHERE a.phone=?")
		    .passwordEncoder(passwordEncoder()).rolePrefix("ROLE_");
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {

		http
			.authorizeRequests()
			.antMatchers("/home", "/register", "/confirm").permitAll()
				.and()	
			.authorizeRequests()
			.antMatchers("/bills/**", "/accounts/show", "/operations/*", "/accounts/password")
			.hasAnyRole("ADMIN", "CLIENT")
			.antMatchers("/accounts/search", "/actuator/**")
			.hasRole("ADMIN")
			.anyRequest().authenticated()
				.and()
			.formLogin()
			.usernameParameter("phone")
			.loginPage("/login")
			.loginProcessingUrl("/auth")
//			.successHandler((request, response, authentication) ->
//			response.sendRedirect("/accounts/show/" + authentication.getName()))
			.defaultSuccessUrl("/success")
            .failureUrl("/login?error")
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
	}
	
}
