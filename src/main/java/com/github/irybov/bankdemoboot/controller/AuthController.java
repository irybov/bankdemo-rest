package com.github.irybov.bankdemoboot.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.github.irybov.bankdemoboot.controller.dto.AccountRequestDTO;
import com.github.irybov.bankdemoboot.controller.dto.AccountResponseDTO;
import com.github.irybov.bankdemoboot.service.AccountService;

@Controller
public class AuthController {
	
	@Autowired
	private AccountService accountService;
	
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
	public String signIn(@ModelAttribute("account") @Valid AccountRequestDTO accountRequestDTO,
			BindingResult result, Model model) {
		
		if(result.hasErrors()) {
			return "/auth/register";
		}
		try {
			accountService.saveAccount(accountRequestDTO);
		} catch (Exception exc) {
			model.addAttribute("message", "This phone number is already in use!");
			return "/auth/register";			
		}
		return "/auth/login";
	}
	
}
