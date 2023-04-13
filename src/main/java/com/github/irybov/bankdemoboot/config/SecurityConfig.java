package com.github.irybov.bankdemoboot.config;

//import javax.sql.DataSource;

//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.github.irybov.bankdemoboot.security.AccountDetailsService;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter{

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
			.authorizeRequests()
			.antMatchers("/home", "/register", "/confirm", "/control", "/webjars/**", "/css/**", "/js/**")
			.permitAll()
				.and()	
			.authorizeRequests()
			.antMatchers("/bills/**", "/accounts/show", "/accounts/password")
			.hasAnyRole("ADMIN", "CLIENT")
			.antMatchers("/accounts/search", "/accounts/status", "/accounts/list/**",
					"/actuator/**", "/operations/**")
			.hasRole("ADMIN")
			.anyRequest().authenticated()
				.and()
			.formLogin()
			.usernameParameter("phone")
			.loginPage("/login")
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
//		http.csrf().disable();
//		http.headers().frameOptions().disable();
	}
	
}
