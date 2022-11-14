package com.github.irybov.bankdemoboot.controller;

import java.util.Currency;
import java.util.HashSet;
import java.util.List;
//import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
//import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

//import com.github.irybov.bankdemoboot.Currency;
import com.github.irybov.bankdemoboot.controller.dto.AccountResponseDTO;
import com.github.irybov.bankdemoboot.controller.dto.BillResponseDTO;
//import com.github.irybov.bankdemoboot.controller.dto.OperationResponseDTO;
import com.github.irybov.bankdemoboot.controller.dto.PasswordRequestDTO;
import com.github.irybov.bankdemoboot.service.OperationService;
import com.github.irybov.bankdemoboot.service.AccountService;
import com.github.irybov.bankdemoboot.service.BillService;

//@Validated
@Controller
public class BankController {

	@Autowired
	@Qualifier("accountServiceAlias")
	private AccountService accountService;
	@Autowired
	@Qualifier("billServiceAlias")
	private BillService billService;

	private final OperationService operationService;
	public BankController(@Qualifier("operationServiceAlias")OperationService operationService) {
		this.operationService = operationService;
	}
		
	private Authentication authentication() {
		return SecurityContextHolder.getContext().getAuthentication();
	}
	
	private final Set<Currency> currencies = new HashSet<>();
//	private final Set<Currency> currencies;
	{
//		currencies = EnumSet.allOf(Currency.class);
		Currency usd = Currency.getInstance("USD");
		currencies.add(usd);
		Currency eur = Currency.getInstance("EUR");
		currencies.add(eur);
		Currency gbp = Currency.getInstance("GBP");
		currencies.add(gbp);
		Currency rub = Currency.getInstance("RUB");
		currencies.add(rub);
	}
	
	@GetMapping("/accounts/show/{phone}")
	public String getAccount(@PathVariable String phone, ModelMap modelMap) {

		String current = authentication().getName();
		if(!accountService.verifyAccount(phone, current)) {
			modelMap.addAttribute("message", "Security restricted information");
			return "forward:/accounts/show/" + current;
		}
		
		AccountResponseDTO account = accountService.getAccountDTO(current);
		List<BillResponseDTO> bills = accountService.getBills(account.getId());
		modelMap.addAttribute("account", account);
		modelMap.addAttribute("bills", bills);
		modelMap.addAttribute("currencies", currencies);		
		return "/account/private";
	}
	
/*	@PostMapping("/accounts/show")
	public String createBill(@RequestParam String currency, ModelMap modelMap) {

		String phone = authentication().getName();
		if(currency.isEmpty()) {
			modelMap.addAttribute("message", "Please choose currency type");
			return getAccount(phone, modelMap);
		}	
		accountService.addBill(phone, currency);
		return "redirect:/accounts/show/{phone}";
	}*/
	
	@PostMapping("/bills/add")
	@ResponseBody
	public BillResponseDTO createBill(@RequestParam Map<String, String> params) {
		
//		if(params.get("currency").isEmpty()) return "Please choose currency type";
//		if(params.get("phone").isEmpty()) phone = authentication().getName();		
		BillResponseDTO bill = accountService.addBill(params.get("phone"), params.get("currency"));
		return bill;
	}
	
	@DeleteMapping("/accounts/show/{phone}")
	public String deleteBill(@PathVariable String phone, @RequestParam int id) {
		billService.deleteBill(id);
		return "redirect:/accounts/show/{phone}";
	}
	
	@DeleteMapping("/bills/delete/{id}")
	@ResponseBody
	public void deleteBill(@PathVariable int id) {
		billService.deleteBill(id);
	}
	
	@PostMapping("/bills/operate")
	public String operateBill(@RequestParam Map<String, String> params, ModelMap modelMap) {
		
		modelMap.addAttribute("id", params.get("id"));
		modelMap.addAttribute("action",  params.get("action"));
		modelMap.addAttribute("balance", params.get("balance"));
		if(params.get("action").equals("transfer")) {
			return "/bill/transfer";
		}
		return "/bill/payment";
	}
	
	@GetMapping(value = "/bills/validate/{id}", produces = MediaType.TEXT_PLAIN_VALUE)
	@ResponseBody
	public String checkOwner(@PathVariable int id) {
		
		BillResponseDTO bill = null;
		try {
			bill = billService.getBillDTO(id);
		}
		catch (Exception exc) {
			return exc.getMessage();
		}		
		return bill.getOwner().getName() + " " + bill.getOwner().getSurname();
	}
	
	@PatchMapping("/bills/launch/{id}")
	public String driveMoney(@PathVariable int id, @RequestParam(required=false) String recipient,
			@RequestParam Map<String, String> params, ModelMap modelMap) {
		
		int target = 0;
		if(recipient != null) {
			if(!recipient.matches("^\\d{1,9}$")) {
				modelMap.addAttribute("id", id);
				modelMap.addAttribute("action", params.get("action"));
				modelMap.addAttribute("balance", params.get("balance"));
				modelMap.addAttribute("message", "Please provide correct bill number");
				return "/bill/transfer";
			}
			else target = Integer.parseInt(recipient);
		}
		
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
/*		if(authentication().getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
			return "redirect:/accounts/search";
		}
		return "redirect:/accounts/show/{phone}";*/
		model.addAttribute("success", "Password changed");
		return "/account/password";
	}
	
/*	@GetMapping("/operations/list")
	public String getOperations(@RequestParam int id, Model model) {
		model.addAttribute("billId", id);
		return "/account/history";
	}
	
	@GetMapping("/operations/list/{id}")
	@ResponseBody
	public Page<OperationResponseDTO> get1page(@PathVariable int id, Pageable pageable) {
		return operationService.getPage(id, pageable);
	}*/
	
}
