package com.github.irybov.bankdemorest.controller;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.PersistenceException;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

//import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
//import org.springframework.validation.annotation.Validated;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.github.irybov.bankdemorest.controller.dto.AccountRequest;
import com.github.irybov.bankdemorest.controller.dto.AccountResponse;
import com.github.irybov.bankdemorest.controller.dto.LoginRequest;
import com.github.irybov.bankdemorest.service.AccountService;
import com.github.irybov.bankdemorest.util.JWTUtility;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@Api(description = "Controller for users authorization and registration")
@Slf4j
//@Validated
@Controller
public class AuthController extends BaseController {
	
	@Autowired
	@Qualifier("accountServiceAlias")
	private AccountService accountService;
	
	@Autowired
	private JWTUtility jwtUtility;
	@Autowired
	private AuthenticationManager authenticationManager;
	@Autowired
	private UserDetailsService accountDetailsService;
	
	@Qualifier("beforeCreateAccountValidator")
	private final Validator accountValidator;
	public AuthController(Validator accountValidator) {
		this.accountValidator = accountValidator;
	}

/*	@ApiOperation("Returns apllication's start html-page")
	@GetMapping("/home")
	public String getStartPage() {
		return "auth/home";
	}
	
	@ApiOperation("Returns registration html-page")
	@GetMapping("/register")
	public String createAccount(Model model) {
		model.addAttribute("account", new AccountRequest());
		return "auth/register";
	}
	
	@ApiOperation("Returns login form html-page")
	@GetMapping("/login")
	public String getLoginForm() {
		return "auth/login";
	}*/
/*	
	@ApiOperation("Returns welcome html-page")
	@GetMapping("/success")
	public String getRegistrationForm(Model model, RedirectAttributes redirectAttributes) {
		
		AccountResponse account;
		try {
			account = accountService.getAccountDTO(authentication().getName());
			model.addAttribute("account", account);
			log.info("User {} has enter the system", account.getPhone());
			return "auth/success";
		}
		catch (PersistenceException exc) {
			log.error(exc.getMessage(), exc);
			redirectAttributes.addFlashAttribute("message", exc.getMessage());
			return "redirect:/home";
		}
	}
*/
	@ApiOperation("Generates JWT")
	@PostMapping(value = "/token", produces = MediaType.TEXT_PLAIN_VALUE)
	@ResponseBody
	public String getToken(@Valid @RequestBody LoginRequest loginRequest, 
			HttpServletResponse response) {
		
		try {
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken
					(loginRequest.getPhone(), loginRequest.getPassword()));
		}
		catch(BadCredentialsException exc) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return exc.getMessage();
		}
		
		UserDetails details = accountDetailsService.loadUserByUsername(loginRequest.getPhone());		
		return jwtUtility.generate(details);		
	}
	
	@ApiOperation("Confirms new account's registration")
	@ApiResponses(value = 
		{@ApiResponse(code = 201, message = "Your account has been created", response = String.class), 
		 @ApiResponse(code = 400, message = "", responseContainer = "List", response = String.class),
		 @ApiResponse(code = 409, message = "", response = String.class)})
	@PostMapping("/confirm")
	public ResponseEntity<?> confirmRegistration(@RequestBody AccountRequest accountRequest,
			BindingResult result) {
		
		accountValidator.validate(accountRequest, result);
		if(result.hasErrors()) {
		    List<String> errors = new ArrayList<String>();
		    List<FieldError> results = result.getFieldErrors();
		    for(FieldError error : results) {
		        errors.add(error.getDefaultMessage());
		    }
			log.warn("{}", results);
			return ResponseEntity.badRequest().body(errors);
		}
		
		try {
			accountService.saveAccount(accountRequest);
			return new ResponseEntity<String>("Your account has been created", HttpStatus.CREATED);
		}
		catch (RuntimeException exc) {
			log.error(exc.getMessage(), exc);
			return new ResponseEntity<String>(exc.getMessage(), HttpStatus.CONFLICT);
		}
	}

}
