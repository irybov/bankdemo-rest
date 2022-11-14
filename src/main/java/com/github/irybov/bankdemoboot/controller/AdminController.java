package com.github.irybov.bankdemoboot.controller;

import java.io.BufferedInputStream;
//import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;
//import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
//import org.springframework.ui.ModelMap;
//import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PatchMapping;
//import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.irybov.bankdemoboot.controller.dto.AccountResponseDTO;
import com.github.irybov.bankdemoboot.controller.dto.BillResponseDTO;
import com.github.irybov.bankdemoboot.controller.dto.OperationResponseDTO;
import com.github.irybov.bankdemoboot.model.OperationPage;
import com.github.irybov.bankdemoboot.service.AccountService;
import com.github.irybov.bankdemoboot.service.BillService;
import com.github.irybov.bankdemoboot.service.OperationService;
import com.opencsv.CSVWriter;

//@Validated
@Controller
public class AdminController {

	@Autowired
	@Qualifier("accountServiceAlias")
	private AccountService accountService;
	@Autowired
	@Qualifier("billServiceAlias")
	private BillService billService;
	
	private final OperationService operationService;
	public AdminController(@Qualifier("operationServiceAlias")OperationService operationService) {
		this.operationService = operationService;
	}

	private Authentication authentication() {
		return SecurityContextHolder.getContext().getAuthentication();
	}
	
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/accounts/search")
	public String searchAccount(@RequestParam(required = false) String phone, Model model) {
		
		AccountResponseDTO admin = accountService.getAccountDTO(authentication().getName());
/*		AccountResponseDTO target = null;
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
		}*/
		model.addAttribute("admin", admin);
		return "/account/search";
	}
	
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/accounts/search/{phone}")
	@ResponseBody
	public AccountResponseDTO searchAccount(@PathVariable String phone) {
		
		if(phone != null) {
			if(!phone.matches("^\\d{10}$")) {
				throw new InputMismatchException("Phone number should be 10 digits length");
			}
		}		
		AccountResponseDTO target = accountService.getAccountDTO(phone);
		if(target == null) {
			throw new EntityNotFoundException("Database exception: " + phone + " not found");
		}
		List<BillResponseDTO> bills = accountService.getBills(target.getId());
		target.setBills(bills);
		return target;
	}
	
/*	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/accounts/list")
	@ResponseBody
	public List<AccountResponseDTO> getClients(){
		return accountService.getAll();
	}*/
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/accounts/list")
	public String getClients(Model model){		
		List<AccountResponseDTO> clients = accountService.getAll();
		model.addAttribute("clients", clients);
		return "/account/clients";
	}
/*	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/accounts/list/all")
	@ResponseBody
	public List<AccountResponseDTO> getClients(){
		return accountService.getAll();
	}*/
	/*	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/accounts/list/{pageNumber}")
	@ResponseBody
	public List<AccountResponseDTO> getClients(@PathVariable int pageNumber){
		return accountService.getAll(pageNumber);
	}*/
	
/*	@PreAuthorize("hasRole('ADMIN')")
	@PatchMapping("/accounts/status/{phone}")
	public String changeAccountStatus(@PathVariable String phone, ModelMap modelMap) {
		
		accountService.changeStatus(phone);
		return searchAccount(phone, modelMap);
	}*/	
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/accounts/status/{id}")
	@ResponseBody
	public String changeAccountStatus(@PathVariable int id) {		
		Boolean status = null;
		status = accountService.changeStatus(id);
		return status.toString();
	}
	
/*	@PreAuthorize("hasRole('ADMIN')")
	@PatchMapping("/bills/status/{phone}")
	public String changeBillStatus(@PathVariable String phone, @RequestParam int id,
			ModelMap modelMap) {
		
		try {
			billService.changeStatus(id);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		return searchAccount(phone, modelMap);
	}*/	
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/bills/status/{id}")
	@ResponseBody
	public String changeBillStatus(@PathVariable int id) {
		
		Boolean status = null;
		try {
			status = billService.changeStatus(id);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		return status.toString();
	}
	
/*	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/operations/list")
	public String showOperations(@RequestParam String phone, @RequestParam int id,
			ModelMap modelMap) {
		
		List<OperationResponseDTO> operations = operationService.getAll(id);
		modelMap.addAttribute("operations", operations);
		modelMap.addAttribute("phone", phone);
		return searchAccount(phone, modelMap);
	}*/	
/*	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/operations/list/{id}")
	@ResponseBody
	public List<OperationResponseDTO> getOperations(@PathVariable int id) {
		List<OperationResponseDTO> operations = operationService.getAll(id);
		return operations;
	}*/
	
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/operations/list")
	public String getOperations() {
		return "/account/history";
	}
	
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/operations/list/{id}")
	@ResponseBody
	public Page<OperationResponseDTO> getPage(@PathVariable int id,
			@RequestParam Optional<Double> minval, @RequestParam Optional<Double> maxval,
			@RequestParam Optional<String> action, OperationPage page) {
		
		return operationService.getPage(id, action.orElse("_"),
				minval.orElse(0.01), maxval.orElse(10000.00), page);
	}
	
	@PreAuthorize("hasRole('ADMIN')")
//	@GetMapping("/operations/print/{id}")
	@GetMapping(value = "/operations/print/{id}",
	produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	@ResponseBody
//	public void export2csv(@PathVariable int id, HttpServletResponse response) throws IOException {
	public Resource export2csv(@PathVariable int id) throws IOException {

		BillResponseDTO bill = null;
		try {
			bill = billService.getBillDTO(id);
		}
		catch (Exception exc) {
			exc.printStackTrace();
		}
		AccountResponseDTO account = bill.getOwner();
		List<OperationResponseDTO> operations = operationService.getAll(id);
		
		List<String[]> data = new ArrayList<>();
		String[] owner = {account.getName(), account.getSurname(), account.getPhone()};
		data.add(owner);
		data.add(new String[0]);
		String[] info = {bill.getCurrency(), String.valueOf(bill.getBalance()), bill.getCreatedAt()
				.toString()};
		data.add(info);
		data.add(new String[0]);
		
		String[] header = {"Action", "Amount", "When", "Recipient", "Sender"};
		data.add(header);
		for(OperationResponseDTO operation : operations) {
			String[] row = {operation.getAction(),
							String.valueOf(operation.getAmount()),
							operation.getCreatedAt().toString(),
							String.valueOf(operation.getRecipient()),
							String.valueOf(operation.getSender())};
			data.add(row);
		}
		
		final String SLASH = System.getProperty("file.separator");
		final String PATH = System.getProperty("user.dir") + SLASH + "files";
		new File(PATH).mkdir();
		final String FILENAME = "Bill-" + id +".csv";
		
        try (CSVWriter writer = new CSVWriter(new BufferedWriter(new FileWriter
        		(new File(PATH, FILENAME))))) {
            writer.writeAll(data);
        }
        catch (IOException exc) {
			exc.printStackTrace();
		}
//        File file = new File(PATH + SLASH + FILENAME);
        return new InputStreamResource(new BufferedInputStream(new FileInputStream
        		(PATH + SLASH + FILENAME)));
/*        response.setContentType("text/csv");
        response.setContentLength((int)file.length());
        response.setHeader("Content-Disposition", "attachment; filename="+FILENAME+"");
        response.getWriter().print(file);*/
	}
	
}
