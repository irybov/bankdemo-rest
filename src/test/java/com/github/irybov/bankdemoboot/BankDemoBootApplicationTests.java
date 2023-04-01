package com.github.irybov.bankdemoboot;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;
import java.util.InputMismatchException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
//@Sql("/test-data-h2.sql")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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
		        .andExpect(model().attributeExists("account"))
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
		        .andExpect(model().attributeExists("account"))
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
				.andExpect(redirectedUrl("http://localhost/login"));
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
				.andExpect(model().attributeExists("account"))
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
				.andExpect(model().attributeExists("account"))
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
				.andExpect(model().attributeExists("account"))
				.andExpect(content().string(containsString("Validator in action!")))
				.andExpect(view().name("/auth/register"));
		}
		
	}
	
	@WithMockUser(username = "0000000000", roles = "ADMIN")
	@Nested
	class AdminControllerTest{
		
	    @Test
		void can_get_admin_html() throws Exception {
	    	
			mockMVC.perform(get("/accounts/search"))
				.andExpect(status().isOk())
				.andExpect(model().size(1))
		        .andExpect(model().attributeExists("admin"))
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
		        .andExpect(model().attributeExists("clients"))
		        .andExpect(view().name("/account/clients"));
		}
	    
		@Test
		void can_change_account_status() throws Exception {
			
			mockMVC.perform(get("/accounts/status/{id}", "0"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("false")));
		}
		
		@Disabled
		@Test
		void can_change_bill_status() throws Exception {
			
			mockMVC.perform(get("/bills/status/{id}", "0"))
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
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.createdAt").exists())
				.andExpect(jsonPath("$.updatedAt").exists())
				.andExpect(jsonPath("$.active").exists())
				.andExpect(jsonPath("$.name").exists())
				.andExpect(jsonPath("$.surname").exists())
				.andExpect(jsonPath("$.phone").exists())
				.andExpect(jsonPath("$.birthday").exists())
				.andExpect(jsonPath("$.bills").exists())
				.andExpect(jsonPath("$.bills").isArray())
				.andExpect(jsonPath("$.bills").isEmpty());
//				.andExpect(jsonPath("$.bills").isNotEmpty());
		}
		
	}
	
}
