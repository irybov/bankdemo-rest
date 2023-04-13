package com.github.irybov.bankdemoboot.controller;

import java.util.Currency;
import java.util.HashSet;
import java.util.List;
//import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
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

import lombok.extern.slf4j.Slf4j;

import com.github.irybov.bankdemoboot.service.AccountService;
import com.github.irybov.bankdemoboot.service.BillService;

@Slf4j
//@Validated
@Controller
public class BankController extends BaseController {

	@Autowired
	@Qualifier("accountServiceAlias")
	private AccountService accountService;
	@Autowired
	@Qualifier("billServiceAlias")
	private BillService billService;
	@Autowired
	@Qualifier("operationServiceAlias")
	private OperationService operationService;

	private Set<Currency> currencies;
	@PostConstruct
	private void init() {
		currencies = new HashSet<>();
		Currency usd = Currency.getInstance("USD");
		currencies.add(usd);
		Currency eur = Currency.getInstance("EUR");
		currencies.add(eur);
		Currency gbp = Currency.getInstance("GBP");
		currencies.add(gbp);
		Currency rub = Currency.getInstance("RUB");
		currencies.add(rub);
	}
	
	@PreAuthorize("hasRole('CLIENT')")
	@GetMapping("/accounts/show/{phone}")
	public String getAccount(@PathVariable String phone, ModelMap modelMap, HttpServletResponse response) {

		String current = authentication().getName();
		if(!current.equals(phone)) {
			modelMap.addAttribute("message", "Security restricted information");
			log.warn("User {} tries to get protected information about {}", current, phone);
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return "forward:/accounts/show/" + current;
		}
/*		try {
			if(!accountService.verifyAccount(phone, current)) {
				modelMap.addAttribute("message", "Security restricted information");
				log.warn("User {} tries to get protected information", current);
				return "forward:/accounts/show/" + current;
			}
		}
		catch (Exception exc) {
			log.error(exc.getMessage(), exc);
		}*/
		
		AccountResponseDTO account = null;
		try {
			account = accountService.getAccountDTO(current);
		}
		catch (EntityNotFoundException exc) {
			log.error(exc.getMessage(), exc);
		}
		List<BillResponseDTO> bills = accountService.getBills(account.getId());
		modelMap.addAttribute("account", account);
		modelMap.addAttribute("bills", bills);
		modelMap.addAttribute("currencies", currencies);
		log.info("User {} has enter own private area", account.getPhone());
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
	@PreAuthorize("hasRole('CLIENT')")
	@PostMapping("/bills/add")
	@ResponseBody
	public BillResponseDTO createBill(@RequestParam Map<String, String> params, HttpServletResponse response) {

		log.info("Client {} creates new {} bill", params.get("phone"), params.get("currency"));
//		if(params.get("currency").isEmpty()) return "Please choose currency type";
//		if(params.get("phone").isEmpty()) phone = authentication().getName();		
		BillResponseDTO bill = null;
		try {
			bill = accountService.addBill(params.get("phone"), params.get("currency"));
		}
		catch (Exception exc) {
			log.error(exc.getMessage(), exc);
		}
		response.setStatus(HttpServletResponse.SC_CREATED);
		return bill;
	}
	
/*	@PreAuthorize("hasRole('CLIENT')")
	@DeleteMapping("/accounts/show/{phone}")
	public String deleteBill(@PathVariable String phone, @RequestParam int id) {
		billService.deleteBill(id);
		return "forward:/accounts/show/{phone}";
	}*/
	
	@PreAuthorize("hasRole('CLIENT')")
	@DeleteMapping("/bills/delete/{id}")
	@ResponseBody
	public void deleteBill(@PathVariable int id) {
		log.info("Client {} deletes bill with id {}", authentication().getName(), id);
		billService.deleteBill(id);
	}
	
	@PreAuthorize("hasRole('CLIENT')")
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
	
	@PreAuthorize("hasRole('CLIENT')")
	@GetMapping(value = "/bills/validate/{id}", produces = MediaType.TEXT_PLAIN_VALUE)
	@ResponseBody
	public String checkOwner(@PathVariable int id, HttpServletResponse response) {
		
		BillResponseDTO bill = null;
		try {
			bill = billService.getBillDTO(id);
		}
		catch (Exception exc) {
			log.error(exc.getMessage(), exc);
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return exc.getMessage();
		}		
		return bill.getOwner().getName() + " " + bill.getOwner().getSurname();
	}
	
	@PreAuthorize("hasRole('CLIENT')")
	@PatchMapping("/bills/launch/{id}")
	public String driveMoney(@PathVariable int id, @RequestParam(required=false) String recipient,
			@RequestParam Map<String, String> params, ModelMap modelMap, HttpServletResponse response) {
		
		String phone = authentication().getName();
		int target = 0;
		if(recipient != null) {
			if(!recipient.matches("^\\d{1,9}$")) {
				log.warn("Sender {} types recipient's bill number {} in a wrong format",
																		phone, recipient);
				modelMap.addAttribute("id", id);
				modelMap.addAttribute("action", params.get("action"));
				modelMap.addAttribute("balance", params.get("balance"));
				modelMap.addAttribute("message", "Please provide correct bill number");
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST); 
				return "/bill/transfer";
			}
			else target = Integer.parseInt(recipient);
		}		
		log.info("User {} performs {} operation with bill {}", phone, params.get("action"), id);
		
		String currency;
		switch(params.get("action")) {
		case "deposit":
			try {
				currency = billService.deposit(id, Double.valueOf(params.get("amount")));
				operationService.deposit
				(Double.valueOf(params.get("amount")), params.get("action"), currency, id);
				log.info("{} has been added", params.get("amount"));
			}
			catch (Exception exc) {
				log.warn(exc.getMessage(), exc);
				modelMap.addAttribute("id", id);
				modelMap.addAttribute("action", params.get("action"));
				modelMap.addAttribute("balance", params.get("balance"));
				modelMap.addAttribute("message", exc.getMessage());
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return "/bill/payment";
			}
			break;
		case "withdraw":
			try {
				currency = billService.withdraw(id, Double.valueOf(params.get("amount")));
				operationService.withdraw
				(Double.valueOf(params.get("amount")), params.get("action"), currency, id);
				log.info("{} has been taken", params.get("amount"));
			}
			catch (Exception exc) {
				log.warn(exc.getMessage(), exc);
				modelMap.addAttribute("id", id);
				modelMap.addAttribute("action", params.get("action"));
				modelMap.addAttribute("balance", params.get("balance"));
				modelMap.addAttribute("message", exc.getMessage());
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return "/bill/payment";
			}
			break;
		case "transfer":
			try {
				currency = billService.transfer(id, Double.valueOf(params.get("amount")), target);
				operationService.transfer
				(Double.valueOf(params.get("amount")), params.get("action"), currency, id, target);
				log.info("{} has been sent to bill {}", params.get("amount"), target);
			}
			catch (Exception exc) {
				log.warn(exc.getMessage(), exc);
				modelMap.addAttribute("id", id);
				modelMap.addAttribute("action", params.get("action"));
				modelMap.addAttribute("balance", params.get("balance"));
				modelMap.addAttribute("message", exc.getMessage());
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return "/bill/transfer";
			}
			break;			
		}
		return "redirect:/accounts/show/" + phone;
	}
	
	@PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN')")
	@GetMapping("/accounts/password/{phone}")
	public String changePassword(@PathVariable String phone, Model model) {
		model.addAttribute("password", new PasswordRequestDTO());
		return "/account/password";
	}
	
	@PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN')")
	@PatchMapping("/accounts/password/{phone}")
	public String changePassword(@PathVariable String phone,
			@ModelAttribute("password") @Valid PasswordRequestDTO passwordRequestDTO,
			BindingResult result, Model model, HttpServletResponse response) {

		if(result.hasErrors()) {
			log.warn(result.getFieldErrors().toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return "/account/password";
		}
		
		try {
			if(!accountService.comparePassword(passwordRequestDTO.getOldPassword(), phone)) {
				log.warn("User {} fails to confirm old password", authentication().getName());
				model.addAttribute("message", "Old password mismatch");
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return "/account/password";
			}
		}
		catch (Exception exc) {
			log.error(exc.getMessage(), exc);
		}
		
		try {
			accountService.changePassword(phone, passwordRequestDTO.getNewPassword());
		}
		catch (Exception exc) {
			log.error(exc.getMessage(), exc);
		}
/*		if(authentication().getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
			return "redirect:/accounts/search";
		}
		return "redirect:/accounts/show/{phone}";*/
		model.addAttribute("success", "Password changed");
		log.info("User {} changes password to a new one", authentication().getName());
		return "/account/password";
	}

	@Override
	String setServiceImpl(String impl) {
		// TODO Auto-generated method stub
		return null;
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
