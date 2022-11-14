package com.github.irybov.bankdemoboot.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

//import com.github.irybov.bankdemoboot.dao.AccountDAO;
import com.github.irybov.bankdemoboot.entity.Account;
import com.github.irybov.bankdemoboot.repository.AccountRepository;

@Service
public class AccountDetailsService implements UserDetailsService {
	
	@Autowired
	private AccountRepository repository;
//	@Autowired
//	private AccountDAO dao;
	
	@Override
	public UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
		
		Account account = repository.findByPhone(phone);
//		Account account = dao.getAccount(phone);
		if(account == null) throw new UsernameNotFoundException("User not found");		
		return new AccountDetails(account);
	}

}
