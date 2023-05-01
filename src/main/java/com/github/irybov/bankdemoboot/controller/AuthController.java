package com.github.irybov.bankdemoboot.controller;

import javax.persistence.PersistenceException;
import javax.servlet.http.HttpServletResponse;

//import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
//import org.springframework.validation.annotation.Validated;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.github.irybov.bankdemoboot.controller.dto.AccountRequestDTO;
import com.github.irybov.bankdemoboot.controller.dto.AccountResponseDTO;
import com.github.irybov.bankdemoboot.service.AccountService;
import com.github.irybov.bankdemoboot.service.AccountServiceDAO;
import com.github.irybov.bankdemoboot.service.AccountServiceJPA;

import lombok.extern.slf4j.Slf4j;

@Slf4j
//@Validated
@Controller
public class AuthController extends BaseController {
	
	@Autowired
//	@Qualifier("accountServiceAlias")
	private AccountService accountService;
	
	@Qualifier("beforeCreateAccountValidator")
	private final Validator accountValidator;
	public AuthController(Validator accountValidator) {
		this.accountValidator = accountValidator;
	}

	@GetMapping("/home")
	public String startPage() {
		return "/auth/home";
	}
		
	@GetMapping("/register")
	public String createAccount(Model model) {
		model.addAttribute("account", new AccountRequestDTO());
		return "/auth/register";
	}
	
	@GetMapping("/login")
	public String loginForm() {
		return "/auth/login";
	}
	
	@GetMapping("/success")
	public String signUp(Model model) {
		
		AccountResponseDTO account;
		try {
			account = accountService.getAccountDTO(authentication().getName());
			model.addAttribute("account", account);
			log.info("User {} has enter the system", account.getPhone());
			return "/auth/success";
		}
		catch (PersistenceException exc) {
			log.error(exc.getMessage(), exc);
			return "redirect:/home";
		}
	}
	
	@PostMapping("/confirm")
	public String signIn(@ModelAttribute("account") AccountRequestDTO accountRequestDTO,
			BindingResult result, Model model, HttpServletResponse response) {
		
		accountValidator.validate(accountRequestDTO, result);
		if(result.hasErrors()) {
			log.warn("{}", result.getFieldErrors().toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return "/auth/register";
		}
		try {
			accountService.saveAccount(accountRequestDTO);
			response.setStatus(HttpServletResponse.SC_CREATED);
			return "/auth/login";
		}
		catch (Exception exc) {
			model.addAttribute("message", exc.getMessage());
			log.error(exc.getMessage(), exc);
			response.setStatus(HttpServletResponse.SC_CONFLICT);
			return "/auth/register";
		}
	}

	@PreAuthorize("hasRole('ADMIN')")
	@Override
	String setServiceImpl(String impl) {
		
		if(impl.equals("JPA")) accountService = context.getBean(AccountServiceJPA.class);
		else if(impl.equals("DAO")) accountService = context.getBean(AccountServiceDAO.class);
		return accountService.getClass().getSimpleName();
	}
	
}
