package com.github.irybov.bankdemorest.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.github.irybov.bankdemorest.dao.AccountDAO;
import com.github.irybov.bankdemorest.entity.Account;
import com.github.irybov.bankdemorest.repository.AccountRepository;
import com.github.irybov.bankdemorest.service.AccountService;
import com.github.irybov.bankdemorest.service.AccountServiceDAO;
import com.github.irybov.bankdemorest.service.AccountServiceJPA;

@Service
public class AccountDetailsService implements UserDetailsService {
	
	@Autowired
	private AccountRepository repository;
	@Autowired
	private AccountDAO dao;
	@Autowired
	private ApplicationContext context;
	
	private AccountService accountService;
		
	@Override
	public UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
		
		accountService = context.getBean(AccountService.class);
		Account account = null;
		if(accountService instanceof AccountServiceJPA) {
			account = repository.findByPhone(phone);			
		}
		else if(accountService instanceof AccountServiceDAO) {
			account = dao.getAccount(phone);
		}
		if(account == null) throw new UsernameNotFoundException("User " + phone + " not found");		
		return new AccountDetails(account);
	}

	public void setServiceImpl(String impl) {
		
		if(impl.equals("JPA")) accountService = context.getBean(AccountServiceJPA.class);
		else if(impl.equals("DAO")) accountService = context.getBean(AccountServiceDAO.class);
//		return accountService.getClass().getSimpleName();
	}
	
}
