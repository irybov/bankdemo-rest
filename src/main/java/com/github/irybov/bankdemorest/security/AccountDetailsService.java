package com.github.irybov.bankdemorest.security;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.irybov.bankdemorest.dao.AccountDAO;
import com.github.irybov.bankdemorest.entity.Account;
import com.github.irybov.bankdemorest.jpa.AccountJPA;
import com.github.irybov.bankdemorest.service.AccountService;
import com.github.irybov.bankdemorest.service.AccountServiceDAO;
import com.github.irybov.bankdemorest.service.AccountServiceJPA;

@Service
public class AccountDetailsService implements UserDetailsService {
	
	@Autowired
	private AccountJPA jpa;
	@Autowired
	private AccountDAO dao;
/*	@Autowired
	@Qualifier("accountServiceAlias")
	private AccountService accountService;*/
	
//	@Value("${bean.service-impl}")
	private String impl;
/*	public AccountDetailsService(@Value("accountService"+"${bean.service-impl}") String impl) {
		this.impl = impl;
	}*/
	@Autowired
	public void setImpl(@Value("${bean.service-impl}") String impl) {this.impl = impl;}
	public String getImpl() {return this.impl;}
		
	@Transactional(readOnly = true, noRollbackFor = Exception.class)
	@Override
	public AccountDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
		
//		accountService = (AccountService) context.getBean("accountServiceAlias");
		Account account = null;
		if(impl.equals("JPA")) {
//		if(accountService instanceof AccountServiceJPA) {
			Optional<Account> optional = jpa.findByPhone(phone);
			account = optional.orElseThrow(() -> 
					new UsernameNotFoundException("User " + phone + " not found"));
		}
		else if(impl.equals("DAO")) {
//		else if(accountService instanceof AccountServiceDAO) {
			account = dao.getWithRoles(phone);
			if(account == null) throw new UsernameNotFoundException("User " + phone + " not found");
		}	
		return new AccountDetails(account);
	}
	
}
