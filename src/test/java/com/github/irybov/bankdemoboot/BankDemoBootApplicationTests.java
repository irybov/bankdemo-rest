package com.github.irybov.bankdemoboot;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.CoreMatchers.any;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.irybov.bankdemoboot.controller.dto.AccountRequestDTO;
import com.github.irybov.bankdemoboot.controller.dto.AccountResponseDTO;
import com.github.irybov.bankdemoboot.controller.dto.OperationRequestDTO;
import com.github.irybov.bankdemoboot.controller.dto.PasswordRequestDTO;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
//@Sql("/test-data-h2.sql")
@AutoConfigureMockMvc
@Transactional
public class BankDemoBootApplicationTests {

	@Autowired
	private MockMvc mockMVC;
	
	private Authentication authentication() {
		return SecurityContextHolder.getContext().getAuthentication();
	}

	@Test
	void contextLoads(ApplicationContext context) {
		assertThat(context).isNotNull();
	}
	
	@WithMockUser(username = "0000000000", roles = "ADMIN")
    @Nested
    class SwaggerAccessTest{
    	
		@Test
		void swagger_allowed() throws Exception {
			
	        mockMVC.perform(get("/swagger-ui/"))
	    		.andExpect(status().isOk());
		}
		
		@WithMockUser(username = "1111111111", roles = "CLIENT")
		@Test
		void swagger_denied() throws Exception {
			
	        mockMVC.perform(get("/swagger-ui/"))
				.andExpect(status().isForbidden());
		}
		
	    @Test
	    void can_get_swagger_html() throws Exception {

	        mockMVC.perform(get("/swagger-ui/index.html"))
	        	.andExpect(status().isOk());
	    }
		
	    @Test
	    void can_get_api_docs() throws Exception {

	        mockMVC.perform(get("/v2/api-docs"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON));
		
		}

	    @Disabled
	    @Test
	    void can_get_swagger_config() throws Exception {

	        mockMVC.perform(get("/v2/api-docs/swagger-config"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON));
		
		}
		
    }	

	@Nested
	class AuthControllerTest{
	
		@Test
		void can_get_start_html() throws Exception {
			
	        mockMVC.perform(get("/home"))
	        	.andExpect(status().isOk())
	        	.andExpect(content().string(containsString("Welcome!")))
	        	.andExpect(view().name("/auth/home"));
		}
		
		@Test
		void can_get_registration_form() throws Exception {

	        mockMVC.perform(get("/register"))
		        .andExpect(status().isOk())
		        .andExpect(content().string(containsString("Registration")))
		        .andExpect(model().size(1))
		        .andExpect(model().attribute("account", any(AccountRequestDTO.class)))
		        .andExpect(view().name("/auth/register"));
		}
	
		@Test
		void can_get_login_form() throws Exception {
			
	        mockMVC.perform(get("/login"))
		        .andExpect(status().isOk())
		        .andExpect(content().string(containsString("Log In")))
		        .andExpect(view().name("/auth/login"));
		}
		
		@WithMockUser(username = "0000000000", roles = "ADMIN")
		@Test
		void can_get_menu_html() throws Exception {
			
			String roles = authentication().getAuthorities().toString();
			assertThat(authentication().getName()).isEqualTo("0000000000");
			assertThat(roles).isEqualTo("[ROLE_ADMIN]");

			mockMVC.perform(get("/success").with(csrf()))
				.andExpect(status().isOk())
				.andExpect(authenticated())
				.andExpect(content().string(containsString("Welcome!")))
				.andExpect(content().string(containsString("Admin Adminsky")))
				.andExpect(content().string(containsString("[ROLE_ADMIN]")))
		        .andExpect(model().size(1))
		        .andExpect(model().attribute("account", any(AccountResponseDTO.class)))
		        .andExpect(view().name("/auth/success"));
		}
		
		@WithMockUser(username = "9999999999")
		@Test
		void entity_not_found() throws Exception {
			
			String phone = authentication().getName();			
			assertThat(phone).isEqualTo("9999999999");
			
			mockMVC.perform(get("/success"))
				.andExpect(authenticated())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/home"));
		}
		
		@Test
		void unauthorized_success() throws Exception {
			mockMVC.perform(get("/success"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/home"));
		}
		@Test
		void unauthorized_confirm() throws Exception {
			mockMVC.perform(post("/confirm")).andExpect(status().isForbidden());
		}
		
		@Test
		void accepted_registration() throws Exception {
			
			mockMVC.perform(post("/confirm").with(csrf())
										 .param("birthday", "1998-12-10")
										 .param("name", "Nia")
										 .param("password", "blackmamba")
										 .param("phone", "4444444444")
										 .param("surname", "Nacci")
						)
				.andExpect(status().isCreated())
				.andExpect(view().name("/auth/login"));
		}
		
		@Test
		void rejected_registration() throws Exception {
			
			mockMVC.perform(post("/confirm").with(csrf())
										 .param("birthday", "")
										 .param("name", "N")
										 .param("password", "superb")
										 .param("phone", "XXL")
										 .param("surname", "N")
						)
				.andExpect(status().isBadRequest())
				.andExpect(model().size(1))
				.andExpect(model().attribute("account", any(AccountRequestDTO.class)))
				.andExpect(model().hasErrors())
				.andExpect(view().name("/auth/register"));
			
			mockMVC.perform(post("/confirm").with(csrf())
										 .param("birthday", LocalDate.now().plusYears(10L).toString())
										 .param("name", "N")
										 .param("password", "superb")
										 .param("phone", "XXL")
										 .param("surname", "N")
						)
				.andExpect(status().isBadRequest())
				.andExpect(model().size(1))
				.andExpect(model().attribute("account", any(AccountRequestDTO.class)))
				.andExpect(model().hasErrors())
				.andExpect(view().name("/auth/register"));
		}
		
		@Test
		void interrupted_registration() throws Exception {
			
			mockMVC.perform(post("/confirm").with(csrf())
										 .param("birthday", LocalDate.now().minusYears(10L).toString())
										 .param("name", "Nia")
										 .param("password", "blackmamba")
										 .param("phone", "4444444444")
										 .param("surname", "Nacci")
						)
				.andExpect(status().isConflict())
				.andExpect(model().size(2))
				.andExpect(model().attribute("account", any(AccountRequestDTO.class)))
				.andExpect(model().attribute("message", "You must be 18+ to register"))
				.andExpect(view().name("/auth/register"));
		}
		
		@Test
		void violated_registration() throws Exception {
			
			mockMVC.perform(post("/confirm").with(csrf())
										 .param("birthday", "1998-12-10")
										 .param("name", "Nia")
										 .param("password", "blackmamba")
										 .param("phone", "0000000000")
										 .param("surname", "Nacci")
						)
				.andExpect(status().isBadRequest())
				.andExpect(model().size(1))
				.andExpect(model().attribute("account", any(AccountRequestDTO.class)))
				.andExpect(content().string(containsString("Validator in action!")))
				.andExpect(view().name("/auth/register"));
		}
		
	}
	
	@WithMockUser(username = "0000000000", roles = "ADMIN")
	@Nested
	@Sql("/test-operations-h2.sql")
	class AdminControllerTest{
		
	    @Test
		void can_get_admin_html() throws Exception {
	    	
			mockMVC.perform(get("/accounts/search"))
				.andExpect(status().isOk())
				.andExpect(model().size(1))
		        .andExpect(model().attribute("admin", any(AccountResponseDTO.class)))
				.andExpect(content().string(containsString("Admin's area")))
		        .andExpect(view().name("/account/search"));
	    }
		
		@Test
		void can_get_history_html() throws Exception {
			
			mockMVC.perform(get("/operations/list"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Operations history")))
				.andExpect(view().name("/account/history"));
		}
		
		@Test
		void can_get_clients_list() throws Exception {
			
			mockMVC.perform(get("/accounts/list"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Clients list")))
				.andExpect(model().size(1))
		        .andExpect(model().attribute("clients", any(List.class)))
		        .andExpect(view().name("/account/clients"));
		}
	    
		@Test
		void can_change_account_status() throws Exception {
			
			mockMVC.perform(get("/accounts/status/{id}", "1"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("false")));
		}

		@Test
		void can_change_bill_status() throws Exception {
			
			mockMVC.perform(get("/bills/status/{id}", "1"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("false")));
		}
				
		@Test
		void input_mismatch_exception() throws Exception {
			
			assertThatThrownBy(() -> mockMVC.perform(get("/accounts/search/{phone}", "XXL"))
					.andExpect(status().isInternalServerError()))
					.hasCause(new InputMismatchException("Phone number should be of 10 digits"));
		}
		
		@Test
		void entity_not_found_exception() throws Exception {
			
			String wrong = "4444444444";
			mockMVC.perform(get("/accounts/search/{phone}", wrong))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.report").value("Account with phone " + wrong + " not found"));
		}
		
		@Test
		void can_get_account_info() throws Exception {
			
			String phone = "0000000000";
			mockMVC.perform(get("/accounts/search/{phone}", phone))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.createdAt").isNotEmpty())
				.andExpect(jsonPath("$.updatedAt").isNotEmpty())
				.andExpect(jsonPath("$.active").isBoolean())
				.andExpect(jsonPath("$.name").value("Admin"))
				.andExpect(jsonPath("$.surname").value("Adminsky"))
				.andExpect(jsonPath("$.phone").value(phone))
				.andExpect(jsonPath("$.birthday").isNotEmpty())
				.andExpect(jsonPath("$.bills").isArray())
				.andExpect(jsonPath("$.bills.length()", is(1)));
		}

		@Test
		void can_get_operations_page() throws Exception {
			
			mockMVC.perform(get("/operations/list/{id}", "1")
							.param("action", "")
							.param("minval", "")
							.param("maxval", "")
							.param("mindate", "")
							.param("maxdate", "")
					)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.pageable").exists())
				.andExpect(jsonPath("$.sort").exists())
				.andExpect(jsonPath("$.content").isArray())
				.andExpect(jsonPath("$.content.length()", is(3)));
		}
		
		@Test
		void can_export_data_2_csv_file() throws Exception {
			
			mockMVC.perform(get("/operations/print/{id}", "1"))
				.andExpect(status().isCreated())
				.andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE));
		}
		
	}
	
	@Sql(statements = "INSERT INTO bankdemo.bills(id, is_active, balance, currency, account_id)"
				 +" "+"VALUES('2', '1', '10.00', 'SEA', '2');")
	@WithMockUser(username = "1111111111", roles = "CLIENT")
	@Nested
	class BankControllerTest{
		
		@Autowired
		private ObjectMapper mapper;
		
		private static final String PHONE = "1111111111";
		
		@Test
		void can_get_client_html() throws Exception {
			
			mockMVC.perform(get("/accounts/show/{phone}", PHONE))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Private area")))
			.andExpect(model().size(3))
			.andExpect(model().attribute("account", any(AccountResponseDTO.class)))
			.andExpect(model().attribute("bills", any(List.class)))
			.andExpect(model().attribute("currencies", any(Set.class)))
			.andExpect(view().name("/account/private"));
		}
		
		@Test
		void check_security_restriction() throws Exception {
			
			mockMVC.perform(get("/accounts/show/{phone}", "5555555555"))
			.andExpect(status().isForbidden())
			.andExpect(model().size(1))
			.andExpect(model().attribute("message", "Security restricted information"))
			.andExpect(forwardedUrl("/accounts/show/" + PHONE));
		}
		
		@Test
		void can_create_new_bill() throws Exception {
			
			mockMVC.perform(post("/bills/add").with(csrf())
										   .param("phone", PHONE)
										   .param("currency", "SEA")
							)
					.andExpect(status().isCreated())
					.andExpect(jsonPath("$.id").isNumber())
					.andExpect(jsonPath("$.createdAt").exists())
					.andExpect(jsonPath("$.updatedAt").isEmpty())
					.andExpect(jsonPath("$.active").value(true))
					.andExpect(jsonPath("$.balance").value("0.0"))
					.andExpect(jsonPath("$.currency").value("SEA"));
//					.andExpect(jsonPath("$.owner").exists());
		}
		
		@Test
		void can_delete_own_bill() throws Exception {
			
			mockMVC.perform(delete("/bills/delete/{id}", "1").with(csrf()))
					.andExpect(status().isOk());
		}
		
		@Test
		void can_get_payment_html() throws Exception {
			
			mockMVC.perform(post("/bills/operate").with(csrf())
											   .param("id", "1")
											   .param("action", "deposit")
											   .param("balance", "0.00")
						)
				.andExpect(status().isOk())
				.andExpect(model().size(3))
				.andExpect(model().attribute("id", "1"))
				.andExpect(model().attribute("action", "deposit"))
				.andExpect(model().attribute("balance", "0.00"))
				.andExpect(view().name("/bill/payment"));
			
			mockMVC.perform(post("/bills/operate").with(csrf())
											   .param("id", "1")
											   .param("action", "withdraw")
											   .param("balance", "0.00")
						)
				.andExpect(status().isOk())
				.andExpect(model().size(3))
				.andExpect(model().attribute("id", "1"))
				.andExpect(model().attribute("action", "withdraw"))
				.andExpect(model().attribute("balance", "0.00"))
				.andExpect(view().name("/bill/payment"));
		}

		@Test
		void can_get_transfer_html() throws Exception {
			
			mockMVC.perform(post("/bills/operate").with(csrf())
											   .param("id", "1")
											   .param("action", "transfer")
											   .param("balance", "0.00")
						)
				.andExpect(status().isOk())
				.andExpect(model().size(3))
				.andExpect(model().attribute("id", "1"))
				.andExpect(model().attribute("action", "transfer"))
				.andExpect(model().attribute("balance", "0.00"))
				.andExpect(view().name("/bill/transfer"));
		}
		
		@Test
		void check_bill_owner() throws Exception {
			
			mockMVC.perform(get("/bills/validate/{id}", "1"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.valueOf("text/plain;charset=UTF-8")))
				.andExpect(content().string(containsString("Admin Adminsky")));
		}
		
		@Test
		void recipient_not_found() throws Exception {
			
			mockMVC.perform(get("/bills/validate/{id}", "4"))
				.andExpect(status().isNotFound())
				.andExpect(content().contentType(MediaType.valueOf("text/plain;charset=UTF-8")))
				.andExpect(content().string(containsString("Target bill with id: 4 not found")));
		}
		
		@Test
		void can_get_password_html() throws Exception {
			
			mockMVC.perform(get("/accounts/password/{phone}", PHONE))
			.andExpect(status().isOk())
			.andExpect(model().size(1))
			.andExpect(model().attribute("password", any(PasswordRequestDTO.class)))
			.andExpect(view().name("/account/password"));
		}
		
		@Disabled
		@Test
		void success_password_change() throws Exception {
			
			mockMVC.perform(patch("/accounts/password/{phone}", PHONE).with(csrf())
										.param("oldPassword", "supervixen")
										.param("newPassword", "japanrocks")
					)
					.andExpect(status().isOk())
					.andExpect(model().size(2))
					.andExpect(model().attributeExists("password"))
					.andExpect(model().attribute("success", "Password changed"))
					.andExpect(view().name("/account/password"));
		}
		
		@Test
		void failure_password_change() throws Exception {
			
			mockMVC.perform(patch("/accounts/password/{phone}", PHONE).with(csrf())
										.param("oldPassword", "superjapan")
										.param("newPassword", "japanrocks")
					)
					.andExpect(status().isBadRequest())
					.andExpect(model().size(2))
					.andExpect(model().attributeExists("password"))
					.andExpect(model().attribute("message", "Old password mismatch"))
					.andExpect(view().name("/account/password"));			
		}
		
		@Test
		void password_binding_errors() throws Exception {
			
			mockMVC.perform(patch("/accounts/password/{phone}", PHONE).with(csrf())
										.param("oldPassword", "vixen")
										.param("newPassword", "japan")
					)
					.andExpect(status().isBadRequest())
					.andExpect(model().size(1))
					.andExpect(model().errorCount(2))
					.andExpect(model().attributeExists("password"))
					.andExpect(view().name("/account/password"));			
		}
		
		@Test
		void wrong_format_input() throws Exception {
			
			mockMVC.perform(patch("/bills/launch/{id}", "1").with(csrf())
										.param("recipient", "XXX")
//									    .param("id", "1")
									    .param("action", "transfer")
									    .param("balance", "10.00")
					)
					.andExpect(status().isBadRequest())
					.andExpect(model().size(4))
					.andExpect(model().attribute("id", 1))
					.andExpect(model().attribute("action", "transfer"))
					.andExpect(model().attribute("balance", "10.00"))
					.andExpect(model().attribute("message", "Please provide correct bill number"))
					.andExpect(view().name("/bill/transfer"));
		}
		
		@Test
		void successful_payment() throws Exception {
			
			mockMVC.perform(patch("/bills/launch/{id}", "1").with(csrf())
//									    .param("id", "1")
									    .param("action", "deposit")
									    .param("balance", "10.00")
									    .param("amount", "10.00")
					)
					.andExpect(status().is3xxRedirection())
					.andExpect(redirectedUrl("/accounts/show/" + PHONE));
			
			mockMVC.perform(patch("/bills/launch/{id}", "1").with(csrf())
//									    .param("id", "1")
									    .param("action", "withdraw")
									    .param("balance", "20.00")
									    .param("amount", "20.00")
					)
					.andExpect(status().is3xxRedirection())
					.andExpect(redirectedUrl("/accounts/show/" + PHONE));
			
			OperationRequestDTO dto = new OperationRequestDTO(777, 2, "SEA", 0.01);
			mockMVC.perform(patch("/bills/external")
													.contentType(MediaType.APPLICATION_JSON)
													.content(mapper.writeValueAsString(dto))
							)
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Successful")));
		}
		
		@Test
		void zero_amount_exception() throws Exception {
			
			mockMVC.perform(patch("/bills/launch/{id}", "1").with(csrf())
//								    .param("id", "1")
								    .param("action", "deposit")
								    .param("balance", "10.00")
								    .param("amount", "0.00")
				)
				.andExpect(status().isInternalServerError())
				.andExpect(model().size(4))
				.andExpect(model().attribute("id", 1))
				.andExpect(model().attribute("action", "deposit"))
				.andExpect(model().attribute("balance", "10.00"))
				.andExpect(model().attribute("message", "Amount of money should be higher than zero"))
				.andExpect(view().name("/bill/payment"));
			
			mockMVC.perform(patch("/bills/launch/{id}", "1").with(csrf())
//								    .param("id", "1")
								    .param("action", "withdraw")
								    .param("balance", "10.00")
								    .param("amount", "0.00")
				)
				.andExpect(status().isInternalServerError())
				.andExpect(model().size(4))
				.andExpect(model().attribute("id", 1))
				.andExpect(model().attribute("action", "withdraw"))
				.andExpect(model().attribute("balance", "10.00"))
				.andExpect(model().attribute("message", "Amount of money should be higher than zero"))
				.andExpect(view().name("/bill/payment"));
			
			OperationRequestDTO dto = new OperationRequestDTO(777, 2, "SEA", 0.00);
			mockMVC.perform(patch("/bills/external")
													.contentType(MediaType.APPLICATION_JSON)
													.content(mapper.writeValueAsString(dto))
							)
				.andExpect(status().isBadRequest())
				.andExpect(content().string(containsString("Amount of money should be higher than zero")));	
		}
		
		@Test
		void negative_balance_exception() throws Exception {
			
			mockMVC.perform(patch("/bills/launch/{id}", "1").with(csrf())
//								    .param("id", "1")
								    .param("action", "withdraw")
								    .param("balance", "10.00")
								    .param("amount", "10.01")
				)
				.andExpect(status().isInternalServerError())
				.andExpect(model().size(4))
				.andExpect(model().attribute("id", 1))
				.andExpect(model().attribute("action", "withdraw"))
				.andExpect(model().attribute("balance", "10.00"))
				.andExpect(model().attribute("message", "Not enough money to complete operation"))
				.andExpect(view().name("/bill/payment"));
		}
		
		@Test
		void bills_id_match_exception() throws Exception {
			
			mockMVC.perform(patch("/bills/launch/{id}", "1").with(csrf())
				    			.param("recipient", "1")
//							    .param("id", "1")
							    .param("action", "transfer")
							    .param("balance", "10.00")
							    .param("amount", "10.00")
			)
			.andExpect(status().isInternalServerError())
			.andExpect(model().size(4))
			.andExpect(model().attribute("id", 1))
			.andExpect(model().attribute("action", "transfer"))
			.andExpect(model().attribute("balance", "10.00"))
			.andExpect(model().attribute("message", "Source and target bills should not be the same"))
			.andExpect(view().name("/bill/transfer"));
			
			OperationRequestDTO dto = new OperationRequestDTO(777, 777, "SEA", 0.01);
			mockMVC.perform(patch("/bills/external")
													.contentType(MediaType.APPLICATION_JSON)
													.content(mapper.writeValueAsString(dto))
							)
				.andExpect(status().isInternalServerError())
				.andExpect(content().string(containsString("Source and target bills should not be the same")));
		}
		
		@Test
		void currency_mismatch_exception() throws Exception {
			
			mockMVC.perform(patch("/bills/launch/{id}", "1").with(csrf())
										.param("recipient", "2")
//										.param("id", "1")
										.param("action", "transfer")
										.param("balance", "10.00")
										.param("amount", "10.00")
					)
					.andExpect(status().isInternalServerError())
					.andExpect(model().size(4))
					.andExpect(model().attribute("id", 1))
					.andExpect(model().attribute("action", "transfer"))
					.andExpect(model().attribute("balance", "10.00"))
					.andExpect(model().attribute("message", "Wrong currency type of the target bill"))
					.andExpect(view().name("/bill/transfer"));
			
			OperationRequestDTO dto = new OperationRequestDTO(777, 2, "AUD", 0.01);
			mockMVC.perform(patch("/bills/external")
													.contentType(MediaType.APPLICATION_JSON)
													.content(mapper.writeValueAsString(dto))
							)
				.andExpect(status().isInternalServerError())
				.andExpect(content().string(containsString("Wrong currency type of the target bill")));
		}
		
		@Test
		void constraint_violation_exception() throws Exception {
			
			OperationRequestDTO dto = new OperationRequestDTO(1_000_000_000, -1, "yuan", -0.01);
			mockMVC.perform(patch("/bills/external")
													.contentType(MediaType.APPLICATION_JSON)
													.content(mapper.writeValueAsString(dto))
							)
				.andExpect(status().isBadRequest())
				.andExpect(content().string(containsString("Sender's bill number should be less than 10 digits length")))
				.andExpect(content().string(containsString("Recepient's bill number should be positive number")))
				.andExpect(content().string(containsString("Currency code should be 3 capital characters length")))
				.andExpect(content().string(containsString("Amount of money should be higher than zero")));		
		}
		
		@Test
		void establish_emitter_connection() throws Exception {			
			mockMVC.perform(get("/bills/notify")).andExpect(status().isOk());
		}
		
	}
	
	@WithMockUser(username = "0000000000", roles = "ADMIN")
	@Nested
	class MegaControllerTest{
		
		@Test
		void can_change_implementations() throws Exception {

			String impl = "DAO";
			String bean = "AccountService".concat(impl);
			
			mockMVC.perform(put("/control").with(csrf()).param("impl", impl))
				.andExpect(status().isOk())
				.andExpect(content()
					.string(containsString("Services impementation has been switched to " + bean)));

			impl = "JPA";
			bean = "AccountService".concat(impl);

			mockMVC.perform(put("/control").with(csrf()).param("impl", impl))
				.andExpect(status().isOk())
				.andExpect(content()
					.string(containsString("Services impementation has been switched to " + bean)));
		}
		
		
		@Test
		void wrong_implementation_type() throws Exception {
			
			String impl = "XXX";
			mockMVC.perform(put("/control").with(csrf()).param("impl", impl))
					.andExpect(status().isBadRequest())
					.andExpect(content()
						.string(containsString("Wrong implementation type, retry")));
		}
		
	}
	
}
