package com.github.irybov.bankdemoboot.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.github.irybov.bankdemoboot.controller.dto.AccountResponseDTO;
import com.github.irybov.bankdemoboot.controller.dto.OperationResponseDTO;
import com.github.irybov.bankdemoboot.service.AccountService;
import com.github.irybov.bankdemoboot.service.BillService;
import com.github.irybov.bankdemoboot.service.OperationService;

@Controller
public class AdminController {

	@Autowired
	private AccountService accountService;
	@Autowired
	private BillService billService;
	@Autowired
	private OperationService operationService;

	private Authentication authentication() {
		return SecurityContextHolder.getContext().getAuthentication();
	}
	
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/accounts/search")
	public String searchAccount(@RequestParam(required = false) String phone,
			@RequestParam(required = false) List<OperationResponseDTO> operations,
			ModelMap modelMap) {
		
		AccountResponseDTO admin = accountService.getAccountDTO(authentication().getName());		
		AccountResponseDTO target = null;
		try {
			target = accountService.getAccountDTO(phone);
		}
		catch (Exception exc) {
			if(phone != null) {
				modelMap.addAttribute("message", "Account with number: " + phone + " not found");
			}
		}
		finally {
			modelMap.addAttribute("admin", admin);
			modelMap.addAttribute("target", target);
			modelMap.addAttribute("operations", operations);
		}
		return "/account/search";
	}
	
	@PreAuthorize("hasRole('ADMIN')")
	@PatchMapping("/accounts/status/{phone}")
	public String changeAccountStatus(@PathVariable String phone,
			@RequestParam(required = false) List<OperationResponseDTO> operations,
			ModelMap modelMap) {
		accountService.changeStatus(phone);
		return searchAccount(phone, operations, modelMap);
	}
	
	@PreAuthorize("hasRole('ADMIN')")
	@PatchMapping("/bills/status/{phone}")
	public String changeBillStatus(@PathVariable String phone,
			@RequestParam(required = false) List<OperationResponseDTO> operations,
			@RequestParam int id,
			ModelMap modelMap) {
		try {
			billService.changeStatus(id);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		return searchAccount(phone, operations, modelMap);
	}
	
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/operations/list")
	public String showOperations(@RequestParam String phone, @RequestParam int id,
			ModelMap modelMap) {		
		List<OperationResponseDTO> operations = operationService.getAll(id);
		modelMap.addAttribute("operations", operations);
		modelMap.addAttribute("phone", phone);
		return searchAccount(phone, operations, modelMap);
	}
	
}
