package com.github.irybov.bankdemoboot.controller;

//import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

//@Validated
@Controller
public class AuthController {
	
	@Autowired
	@Qualifier("accountServiceAlias")
	private AccountService accountService;
	
	@Qualifier("beforeCreateAccountValidator")
	private final Validator accountValidator;
	public AuthController(Validator accountValidator) {
		this.accountValidator = accountValidator;
	}
	
	private Authentication authentication() {
		return SecurityContextHolder.getContext().getAuthentication();
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
		AccountResponseDTO account = accountService.getAccountDTO(authentication().getName());
		model.addAttribute("account", account);
		return "/auth/success";
	}
	
	@PostMapping("/confirm")
	public String signIn(@ModelAttribute("account") AccountRequestDTO accountRequestDTO,
			BindingResult result, Model model) {
		
		accountValidator.validate(accountRequestDTO, result);
		if(result.hasErrors()) {
			return "/auth/register";
		}
		try {
			accountService.saveAccount(accountRequestDTO);
		}
		catch (Exception exc) {
			model.addAttribute("message", exc.getMessage());
			return "/auth/register";			
		}
		return "/auth/login";
	}
	
}
