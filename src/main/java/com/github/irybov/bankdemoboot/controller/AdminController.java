package com.github.irybov.bankdemoboot.controller;

import java.io.BufferedInputStream;
//import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.FileWriter;
import java.io.IOException;
//import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
//import java.io.PrintStream;
import java.io.PrintWriter;
//import java.nio.file.FileSystems;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
import java.util.stream.Stream;

import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceException;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.irybov.bankdemoboot.controller.dto.AccountResponseDTO;
import com.github.irybov.bankdemoboot.controller.dto.BillResponseDTO;
import com.github.irybov.bankdemoboot.controller.dto.OperationResponseDTO;
import com.github.irybov.bankdemoboot.model.OperationPage;
import com.github.irybov.bankdemoboot.service.AccountService;
import com.github.irybov.bankdemoboot.service.BillService;
import com.github.irybov.bankdemoboot.service.OperationService;
import com.opencsv.CSVWriter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
//@Validated
@Controller
public class AdminController {

	@Autowired
	@Qualifier("accountServiceAlias")
	private AccountService accountService;
	@Autowired
	@Qualifier("billServiceAlias")
	private BillService billService;
	@Autowired
	@Qualifier("operationServiceAlias")
	private OperationService operationService;
	
	private final Executor executorService;
	public AdminController(@Qualifier("asyncExecutor")Executor executorService) {
		this.executorService = executorService;
	}

	private Authentication authentication() {
		return SecurityContextHolder.getContext().getAuthentication();
	}
	
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/accounts/search")
	public String searchAccount(@RequestParam(required = false) String phone, Model model) {
		
		AccountResponseDTO admin;
		try {
			admin = accountService.getAccountDTO(authentication().getName());
			model.addAttribute("admin", admin);
			log.info("Admin {} has enter admin's zone", admin.getPhone());
		}
		catch (PersistenceException exc) {
			log.error(exc.getMessage(), exc);
		}
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
		return "/account/search";
	}
	
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/accounts/search/{phone}")
//	@ResponseBody
	public ResponseEntity<?> searchAccount(@PathVariable String phone) {
		
		if(phone != null) {
			if(!phone.matches("^\\d{10}$")) {
				log.warn("Admin {} types phone {} in a wrong format",
								  authentication().getName(), phone);
				throw new InputMismatchException("Phone number should be of 10 digits");
			}
		}
		
		AccountResponseDTO target = null;
		try {
			target = accountService.getAccountDTO(phone);
			List<BillResponseDTO> bills = accountService.getBills(target.getId());
			target.setBills(bills);
			log.info("Admin {} requests data about client {}", authentication().getName(), phone);
			return new ResponseEntity<AccountResponseDTO>(target, HttpStatus.OK);
		}
		catch (PersistenceException exc) {
			log.error("Database exception: account with phone {} not found", phone, exc);
			String message = "Account with phone " + phone + " not found";
			
			ObjectMapper objectMapper = new ObjectMapper();
			Map<String, String> map = Stream.of(new String[][] {{"report", message},})
							.collect(Collectors.toMap(data -> data[0], data -> data[1]));
			String json = null;
			try {
				json = objectMapper.writeValueAsString(map);
			}
			catch (JsonProcessingException jpe) {
				jpe.printStackTrace();
			}
			return new ResponseEntity<String>(json, HttpStatus.NOT_FOUND);
//			return new ResponseEntity<String>(message, HttpStatus.NOT_FOUND);
		}
/*		if(target == null) {
			log.error("Database exception: " + phone + " not found");
			throw new EntityNotFoundException("Database exception: " + phone + " not found");
		}*/
//		List<BillResponseDTO> bills = accountService.getBills(target.getId());
//		target.setBills(bills);
//		return target;
//		return new ResponseEntity<>(target, HttpStatus.OK);
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
		log.info("Admin {} changes active status of client {} to {}",
							 authentication().getName(), id, status);
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
		status = billService.changeStatus(id);
		log.info("Admin {} changes active status of bill {} to {}",
						   authentication().getName(), id, status);
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
			@RequestParam Optional<String> mindate, @RequestParam Optional<String> maxdate,
			@RequestParam Optional<Double> minval, @RequestParam Optional<Double> maxval,
			@RequestParam Optional<String> action, Pageable pageable) {
		
		final String FROM = mindate.orElse("1900-01-01");
		final String TO = maxdate.orElse(LocalDate.now().toString());
		LocalDate from = LocalDate.parse(FROM.isEmpty() ? "1900-01-01" : FROM);
		LocalDate to = LocalDate.parse(TO.isEmpty() ? LocalDate.now().toString() : TO);
		
		OffsetDateTime dateFrom = OffsetDateTime.of(from, LocalTime.MIN, ZoneOffset.UTC);
		OffsetDateTime dateTo = OffsetDateTime.of(to, LocalTime.MAX, ZoneOffset.UTC);
		
		OperationPage page = new OperationPage();
		page.setPageNumber(pageable.getPageNumber());
		page.setPageSize(pageable.getPageSize());
		
		log.info("Admin {} requests list of operations with bill {}",
		authentication().getName(), id);
		
		return operationService.getPage(id, action.orElse("_"),
				minval.orElse(0.01), maxval.orElse(10000.00), dateFrom, dateTo, page);
	}
	
	@PreAuthorize("hasRole('ADMIN')")
//	@GetMapping("/operations/print/{id}")
	@GetMapping(value = "/operations/print/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	@ResponseBody
//	public void export2csv(@PathVariable int id, HttpServletResponse response) throws IOException {
	public Resource export2csv(@PathVariable int id, HttpServletResponse response) {

		List<String[]> data = new ArrayList<>();

		CompletableFuture<List<OperationResponseDTO>> futureOperations =
				CompletableFuture.supplyAsync(() -> operationService.getAll(id), executorService);
		CompletableFuture<BillResponseDTO> futureBill = CompletableFuture.supplyAsync
				(() -> {try {return billService.getBillDTO(id);}
					   	catch(EntityNotFoundException exc) {log.error(exc.getMessage(), exc);
					   	throw new CompletionException(exc);}
				}, executorService);
		
//		BillResponseDTO bill = billService.getBillDTO(id);
		BillResponseDTO bill = futureBill.join();
		AccountResponseDTO account = bill.getOwner();
		
		String[] owner = {account.getName(), account.getSurname(), account.getPhone()};		
		data.add(owner);
		data.add(new String[0]);
		
		String[] info = {bill.getCurrency(), String.valueOf(bill.getBalance()),
						 bill.getCreatedAt().toString()};
		data.add(info);
		data.add(new String[0]);
		
		String[] header = {"Action", "Amount", "When", "Recipient", "Sender"};
		data.add(header);
		data.add(new String[0]);		
		
		List<OperationResponseDTO> operations = futureOperations.join();
		for(OperationResponseDTO operation : operations) {
			String[] row = {operation.getAction(),
							String.valueOf(operation.getAmount()),
							operation.getCreatedAt().toString(),
							String.valueOf(operation.getRecipient()),
							String.valueOf(operation.getSender())};
			data.add(row);
		}
		
/*		final String SLASH = FileSystems.getDefault().getSeparator();
		final String PATH = System.getProperty("user.dir") + SLASH + "files";
		new File(PATH).mkdir();
		final String FILENAME = "Bill-" + id +".csv";*/
		
		byte[] byteArray = null;
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
				OutputStreamWriter osw = new OutputStreamWriter(baos);
				PrintWriter pw = new PrintWriter(osw);
				BufferedWriter bw = new BufferedWriter(pw);
				CSVWriter writer = new CSVWriter(bw);) {
			writer.writeAll(data);
			writer.flush();
			byteArray = baos.toByteArray();
		}
        catch (IOException exc) {
        	log.error(exc.getMessage(), exc);
		}
		
/*        try (CSVWriter writer = new CSVWriter(new BufferedWriter(new FileWriter
        		(new File(PATH, FILENAME))))) {
            writer.writeAll(data);
        }
        catch (IOException exc) {
        	log.error(exc.getMessage(), exc);
		}*/
		response.setStatus(HttpServletResponse.SC_CREATED);        		
		log.info("Admin {} exports data about bill {} to csv", authentication().getName(), id);		
        return new InputStreamResource(new BufferedInputStream(new ByteArrayInputStream(byteArray)));
        
//		File file = new File(PATH + SLASH + FILENAME);
//		return new InputStreamResource(new BufferedInputStream(new FileInputStream(file)));
/*        response.setContentType("text/csv");
        response.setContentLength((int)file.length());
        response.setHeader("Content-Disposition", "attachment; filename="+FILENAME+"");
        response.getWriter().print(file);*/
	}
	
}
