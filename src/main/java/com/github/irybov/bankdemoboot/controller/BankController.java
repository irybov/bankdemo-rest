package com.github.irybov.bankdemoboot.controller;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.core.GrantedAuthority;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.github.irybov.bankdemoboot.CurrencyType;
import com.github.irybov.bankdemoboot.controller.dto.AccountRequestDTO;
import com.github.irybov.bankdemoboot.controller.dto.AccountResponseDTO;
import com.github.irybov.bankdemoboot.entity.Operation;
import com.github.irybov.bankdemoboot.service.BillService;
import com.github.irybov.bankdemoboot.service.OperationService;
import com.github.irybov.bankdemoboot.service.AccountService;

@Controller
@RequestMapping("/bankdemo")
@Validated
public class BankController {

	@Autowired
	private AccountService accountService;
	@Autowired
	private BillService billService;
	@Autowired
	private OperationService operationService;
	
	private Set<CurrencyType> currencies;
	{
		currencies = EnumSet.allOf(CurrencyType.class);
	}
	
	private String getPrincipalName() {
		return SecurityContextHolder.getContext().getAuthentication().getName();
	}
	private String currentURL = null;
	
	@GetMapping("/home")
	public String startPage() {
		return "home";
	}
		
	@GetMapping("/register")
	public String createAccount(Model model) {
		model.addAttribute("account", new AccountRequestDTO());
		return "register";
	}
	
	@GetMapping("/login")
	public String loginForm() {
		return "login";
	}
	
	@GetMapping("/success")
	public String signUp(Model model) {		
		AccountResponseDTO account = accountService.getAccountDTO(getPrincipalName());
		model.addAttribute("account", account);
		return "success";
	}
	
	@PostMapping("/confirm")
	public String signIn(@ModelAttribute("account") @Valid AccountRequestDTO accountRequestDTO,
			BindingResult result, Model model) {
		
		if(result.hasErrors()) {
			return "register";
		}
		try {
			accountService.saveAccount(accountRequestDTO);
		} catch (Exception exc) {
			model.addAttribute("message", exc.getMessage());
			return "register";			
		}
		return "login";
	}
	
	@GetMapping("/accounts/show/{phone}")
	public String getAccount(@PathVariable String phone, ModelMap modelMap) {

		String current = getPrincipalName();		
		AccountResponseDTO account = accountService.getAccountDTO(current);
		modelMap.addAttribute("account", account);
		modelMap.addAttribute("currencies", currencies);
		
		if(!accountService.verifyAccount(phone, current)) {
			modelMap.addAttribute("message", "Security restricted information");
			return "forward:/bankdemo/accounts/show/" + current;
		}
		return "private";
	}
	
	@PostMapping("/accounts/show/{phone}")
	public String createBill(@PathVariable String phone, @RequestParam String currency,
			ModelMap modelMap) {

		if(currency.isEmpty()) {
			modelMap.addAttribute("message", "Please choose currency type");
			return getAccount(phone, modelMap);
		}	
		accountService.addBill(phone, currency);
		return "redirect:/bankdemo/accounts/show/{phone}";
	}
	
	@Value("${admin.url}")
	private String adminURL;
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/accounts/search")
	public String searchAccount(@RequestParam(required = false) String phone,
			@RequestParam(required = false) List<Operation> operations, ModelMap modelMap) {

		currentURL = adminURL;
		
		AccountResponseDTO admin = accountService.getAccountDTO(getPrincipalName());		
		AccountResponseDTO target = null;
		try {
			target = accountService.getAccountDTO(phone);
		}
		catch (Exception exc) {
			if(phone != null) {
				modelMap.addAttribute("message", "Account number: " + phone + " not found");
			}
		}
		finally {
			modelMap.addAttribute("admin", admin);
			modelMap.addAttribute("target", target);
			modelMap.addAttribute("operations", operations);
		}
		return "search";
	}
	
	@PreAuthorize("hasRole('ADMIN')")
	@PatchMapping("/accounts/status/{phone}")
	public String changeAccountStatus(@PathVariable String phone,
			@RequestParam(required = false) List<Operation> operations, ModelMap modelMap) {
		accountService.changeStatus(phone);
		return searchAccount(phone, operations, modelMap);
	}
	
	@PreAuthorize("hasRole('ADMIN')")
	@PatchMapping("/bills/status/{phone}")
	public String changeBillStatus(@PathVariable String phone,
			@RequestParam(required = false) List<Operation> operations, @RequestParam int id,
			ModelMap modelMap) {
		billService.changeStatus(id);
		return searchAccount(phone, operations, modelMap);
	}
	
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/operations/list")
	public String showOperations(@RequestParam String phone, @RequestParam int id,
			ModelMap modelMap) {		
		List<Operation> operations = operationService.getAll(id);
		modelMap.addAttribute("operations", operations);
		modelMap.addAttribute("phone", phone);
		return searchAccount(phone, operations, modelMap);
	}
	
	@DeleteMapping("/accounts/show/{phone}")
	public String deleteBill(@PathVariable String phone, @RequestParam int id) {
		billService.deleteBill(id);
		return "redirect:/bankdemo/accounts/show/{phone}";
	}	
	
	@PatchMapping("/bills/operate/{id}")
	public String operateBill(@PathVariable int id, @RequestParam String action,
			@RequestParam double balance, ModelMap modelMap) {
		
		modelMap.addAttribute("id", id);
		modelMap.addAttribute("action", action);
		modelMap.addAttribute("balance", balance);
		if(action.equals("transfer")) {
			return "transfer";
		}
		return "payment";
	}
	
	@PatchMapping("/bills/launch/{id}")
	public String driveMoney(@PathVariable int id, @RequestParam(required = false) Integer to,
			@RequestParam Map<String, String> params, ModelMap modelMap) {
		
		String currency;
		
		switch(params.get("action")) {
		case "deposit":
			currency = billService.deposit(id, Double.valueOf(params.get("amount")));
			operationService.deposit
			(Double.valueOf(params.get("amount")), params.get("action"), currency, id);
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
				return "payment";
			}
			break;
		case "transfer":
			try {
				currency = billService.transfer(id, Double.valueOf(params.get("amount")), to);
				operationService.transfer
				(Double.valueOf(params.get("amount")), params.get("action"), currency, id, to);
			}
			catch (Exception exc) {
				modelMap.addAttribute("id", id);
				modelMap.addAttribute("action", params.get("action"));
				modelMap.addAttribute("balance", params.get("balance"));
				modelMap.addAttribute("message", exc.getMessage());
				return "transfer";
			}
			break;			
		}	
		String phone = billService.getPhone(id);
		return "redirect:/bankdemo/accounts/show/" + phone;
	}
	
	@GetMapping("/accounts/password/{phone}")
	public String changePassword(@PathVariable String phone) {
		return "password";
	}
	
	@PatchMapping("/accounts/password/{phone}")
	public String changePassword(@PathVariable String phone, @RequestParam String oldPassword,
			@NotBlank @Size(min=10, max=50) @RequestParam String newPassword, Model model) {

		if(!accountService.comparePassword(oldPassword, phone)) {
			model.addAttribute("message", "Old password mismatch");
			return "password";
		}
		
		if(currentURL!=null && currentURL.equals(adminURL)) {
			accountService.changePassword(phone, newPassword);
			return "redirect:/bankdemo/accounts/search";
		}		
		accountService.changePassword(phone, newPassword);
		return "redirect:/bankdemo/accounts/show/{phone}";
	}
	
}
