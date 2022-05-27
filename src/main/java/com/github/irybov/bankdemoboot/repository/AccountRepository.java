package com.github.irybov.bankdemoboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.irybov.bankdemoboot.entity.Account;

public interface AccountRepository extends JpaRepository<Account, String> {

}
