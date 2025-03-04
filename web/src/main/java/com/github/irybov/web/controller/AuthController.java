package com.github.irybov.web.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import javax.persistence.PersistenceException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.irybov.service.dto.AccountRequest;
import com.github.irybov.service.dto.AccountResponse;
import com.github.irybov.service.dto.LoginRequest;
import com.github.irybov.service.security.AccountDetails;
import com.github.irybov.service.security.AccountDetailsService;
import com.github.irybov.service.security.EmailService;
import com.github.irybov.service.service.AccountService;
import com.github.irybov.web.security.JWTUtility;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@Api(description = "Controller for users authorization and registration")
@Slf4j
@Validated
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
	private AccountDetailsService accountDetailsService;
	
	@Qualifier("beforeCreateAccountValidator")
	private final Validator accountValidator;
	public AuthController(Validator accountValidator) {
		this.accountValidator = accountValidator;
	}
	
	@Autowired
	private EmailService emailService;
	private Map<String, AccountRequest> accounts = new ConcurrentReferenceHashMap<>();
	@Autowired
	private Cache<String, LoginRequest> cache;

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
	@ApiOperation("Sends OTP to email")
	@ApiResponses(value = 
		{@ApiResponse(code = 200, message = "", response = String.class), 
		 @ApiResponse(code = 400, message = "", responseContainer = "List", response = String.class), 
		 @ApiResponse(code = 401, message = "", response = String.class), 
		 @ApiResponse(code = 404, message = "", response = String.class)})
	@PostMapping("/login")
	@ResponseBody
	public String getCode(@Valid @RequestBody LoginRequest loginRequest) {
		
		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken
				 (loginRequest.getPhone(), loginRequest.getPassword()));
		
		AccountDetails details = accountDetailsService.loadUserByUsername(loginRequest.getPhone());
		String code = emailService.sendVerificationCode(details.getAccount().getEmail());
		cache.put(code, loginRequest);
		return "Check your email";
	}
	
	@ApiOperation("Returns JWT")
	@ApiResponses(value = 
		{@ApiResponse(code = 200, message = "", response = String.class), 
		 @ApiResponse(code = 417, message = "", response = String.class)})
	@GetMapping("/token")
	@ResponseBody
	public String getToken(@AuthenticationPrincipal UserDetails userDetails) {		
		return jwtUtility.generate(userDetails);
	}
	
	@ApiOperation("Registers new account")
	@ApiResponses(value = 
		{@ApiResponse(code = 200, message = "Check you email", response = String.class), 
		 @ApiResponse(code = 400, message = "", responseContainer = "List", response = String.class), 
		 @ApiResponse(code = 500, message = "", response = String.class)})
	@PostMapping("/register")
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
		
		String key = null;
		try {
			key = emailService.sendActivationLink(accountRequest.getEmail());
		}
		catch (MessagingException exc) {
			log.error(exc.getMessage(), exc);
			return ResponseEntity.internalServerError().body(exc.getMessage());
		}
		accounts.putIfAbsent(key, accountRequest);
		return ResponseEntity.ok("Check your email");
/*		
		try {
			accountService.saveAccount(accountRequest);
			return new ResponseEntity<String>("Your account has been created", HttpStatus.CREATED);
		}
		catch (RuntimeException exc) {
			log.error(exc.getMessage(), exc);
			return new ResponseEntity<String>(exc.getMessage(), HttpStatus.CONFLICT);
		}*/
	}

	@ApiOperation("Acivates account by email link")
	@ApiResponses(value = 
		{@ApiResponse(code = 201, message = "Your account has been created", response = String.class), 
		 @ApiResponse(code = 400, message = "", responseContainer = "List", response = String.class),
		 @ApiResponse(code = 409, message = "", response = String.class),
		 @ApiResponse(code = 410, message = "Link has been expired, try to register again", response = String.class)})
	@GetMapping("/activate/{tail}")
//	@Validated
	public ResponseEntity<String> activateAccount(@PathVariable 
			@NotBlank(message = "Path variable must not be blank") 
			@Size(min=8, max=8, message = "Path variable should be 8 chars length") String tail) {
		
		if(accounts.containsKey(tail)) {
			AccountRequest accountRequest = accounts.get(tail);
		
			try {
				accountService.saveAccount(accountRequest);
				accounts.remove(tail);
				return new ResponseEntity<String>("Your account has been created", HttpStatus.CREATED);
			}
			catch (RuntimeException exc) {
				log.error(exc.getMessage(), exc);
				accounts.remove(tail);
				return new ResponseEntity<String>(exc.getMessage(), HttpStatus.CONFLICT);
			}
		}
		
		return new ResponseEntity<String>("Link has been expired, try to register again", HttpStatus.GONE);
	}

}
