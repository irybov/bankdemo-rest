package com.github.irybov.bankdemoboot.controller;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.github.irybov.bankdemoboot.CurrencyType;
import com.github.irybov.bankdemoboot.controller.dto.AccountResponseDTO;
import com.github.irybov.bankdemoboot.controller.dto.PasswordRequestDTO;
import com.github.irybov.bankdemoboot.service.BillServiceImpl;
import com.github.irybov.bankdemoboot.service.OperationServiceImpl;
import com.github.irybov.bankdemoboot.service.AccountServiceImpl;

//@Validated
@Controller
public class BankController {

	@Autowired
	private AccountServiceImpl accountService;
	@Autowired
	private BillServiceImpl billService;
	@Autowired
	private OperationServiceImpl operationService;
	
	private Authentication authentication() {
		return SecurityContextHolder.getContext().getAuthentication();
	}
	
	private Set<CurrencyType> currencies;
	{
		currencies = EnumSet.allOf(CurrencyType.class);
	}
	
	@GetMapping("/accounts/show/{phone}")
	public String getAccount(@PathVariable String phone, ModelMap modelMap) {

		String current = authentication().getName();		
		AccountResponseDTO account = accountService.getAccountDTO(current);
		modelMap.addAttribute("account", account);
		modelMap.addAttribute("currencies", currencies);
		
		if(!accountService.verifyAccount(phone, current)) {
			modelMap.addAttribute("message", "Security restricted information");
			return "forward:/accounts/show/" + current;
		}
		return "/account/private";
	}
	
	@PostMapping("/accounts/show/{phone}")
	public String createBill(@PathVariable String phone, @RequestParam String currency,
			ModelMap modelMap) {

		if(currency.isEmpty()) {
			modelMap.addAttribute("message", "Please choose currency type");
			return getAccount(phone, modelMap);
		}	
		accountService.addBill(phone, currency);
		return "redirect:/accounts/show/{phone}";
	}
	
	@DeleteMapping("/accounts/show/{phone}")
	public String deleteBill(@PathVariable String phone, @RequestParam int id) {
		billService.deleteBill(id);
		return "redirect:/accounts/show/{phone}";
	}	
	
	@PatchMapping("/bills/operate/{id}")
	public String operateBill(@PathVariable int id, @RequestParam String action,
			@RequestParam double balance, ModelMap modelMap) {
		
		modelMap.addAttribute("id", id);
		modelMap.addAttribute("action", action);
		modelMap.addAttribute("balance", balance);
		if(action.equals("transfer")) {
			return "bill/transfer";
		}
		return "/bill/payment";
	}
	
	@PatchMapping("/bills/launch/{id}")
	public String driveMoney(@PathVariable int id, @RequestParam(required = false) Integer target,
			@RequestParam Map<String, String> params, ModelMap modelMap) {
		
		String currency;
		
		switch(params.get("action")) {
		case "deposit":
			try {
				currency = billService.deposit(id, Double.valueOf(params.get("amount")));
				operationService.deposit
				(Double.valueOf(params.get("amount")), params.get("action"), currency, id);
			}
			catch (Exception exc) {
				modelMap.addAttribute("id", id);
				modelMap.addAttribute("action", params.get("action"));
				modelMap.addAttribute("balance", params.get("balance"));
				modelMap.addAttribute("message", exc.getMessage());
				return "/bill/payment";
			}
			break;
		case "withdraw":
			try {
				currency = billService.withdraw(id, Double.valueOf(params.get("amount")));
				operationService.withdraw
				(Double.valueOf(params.get("amount")), params.get("action"), currency, id);
			}
			catch (Exception exc) {
				modelMap.addAttribute("id", id);
				modelMap.addAttribute("action", params.get("action"));
				modelMap.addAttribute("balance", params.get("balance"));
				modelMap.addAttribute("message", exc.getMessage());
				return "/bill/payment";
			}
			break;
		case "transfer":
			try {
				currency = billService.transfer(id, Double.valueOf(params.get("amount")), target);
				operationService.transfer
				(Double.valueOf(params.get("amount")), params.get("action"), currency, id, target);
			}
			catch (Exception exc) {
				modelMap.addAttribute("id", id);
				modelMap.addAttribute("action", params.get("action"));
				modelMap.addAttribute("balance", params.get("balance"));
				modelMap.addAttribute("message", exc.getMessage());
				return "/bill/transfer";
			}
			break;			
		}	
		String phone = authentication().getName();
		return "redirect:/accounts/show/" + phone;
	}
	
	@GetMapping("/accounts/password/{phone}")
	public String changePassword(@PathVariable String phone, Model model) {
		model.addAttribute("password", new PasswordRequestDTO());
		return "/account/password";
	}
	
	@PatchMapping("/accounts/password/{phone}")
	public String changePassword(@PathVariable String phone,
			@ModelAttribute("password") @Valid PasswordRequestDTO passwordRequestDTO,
			BindingResult result, Model model) {

		if(!accountService.comparePassword(passwordRequestDTO.getOldPassword(), phone)) {
			model.addAttribute("message", "Old password mismatch");
			return "/account/password";
		}
		if(result.hasErrors()) {
			return "/account/password";
		}
		
		accountService.changePassword(phone, passwordRequestDTO.getNewPassword());
		if(authentication().getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
			return "redirect:/accounts/search";
		}
		return "redirect:/accounts/show/{phone}";
	}
	
}
