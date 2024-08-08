package com.github.irybov.bankdemorest.controller;

import static org.hamcrest.CoreMatchers.any;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.github.irybov.bankdemorest.config.SecurityBeans;
import com.github.irybov.bankdemorest.config.SecurityConfig;
import com.github.irybov.bankdemorest.controller.AdminController;
import com.github.irybov.bankdemorest.controller.AuthController;
import com.github.irybov.bankdemorest.controller.BankController;
import com.github.irybov.bankdemorest.controller.MegaController;
import com.github.irybov.bankdemorest.security.AccountDetailsService;
import com.github.irybov.bankdemorest.service.AccountService;
import com.github.irybov.bankdemorest.service.AccountServiceDAO;
import com.github.irybov.bankdemorest.service.AccountServiceJPA;

@Disabled
@WebMvcTest(MegaController.class)
@WithMockUser(username = "0000000000", roles = "ADMIN")
@Import(value = {SecurityConfig.class, SecurityBeans.class})
class MegaControllerTest {

	@MockBean
	@Qualifier("accountServiceAlias")
	private AccountService accountService;
	@MockBean
	private AccountServiceJPA accountServiceJPA;
	@MockBean
	private AccountServiceDAO accountServiceDAO;
	@Autowired
	ApplicationContext context;
	@Autowired
	private MockMvc mockMVC;
	
	@Test
	void can_change_implementation() throws Exception {

		String impl = "DAO";
//		String bean = accountService.getClass().getSimpleName();
		when(context.getBean("accountServiceAlias")).thenReturn(accountServiceDAO);
		mockMVC.perform(put("/control").with(csrf()).param("impl", impl))
				.andExpect(status().isOk())
				.andExpect(content()
					.string(containsString("Services impementation has been switched to " + impl)));
		verify(context).getBean(anyString());
		
		impl = "JPA";
//		bean = accountService.getClass().getSimpleName();
		when(context.getBean("accountServiceAlias")).thenReturn(accountServiceJPA);
		mockMVC.perform(put("/control").with(csrf()).param("impl", impl))
				.andExpect(status().isOk())
				.andExpect(content()
					.string(containsString("Services impementation has been switched to " + impl)));
		verify(context).getBean(anyString());
	}
	
	@Test
	void wrong_implementation_type() throws Exception {
		
		String impl = "XXX";
		mockMVC.perform(put("/control").with(csrf()).param("impl", impl))
				.andExpect(status().isBadRequest())
				.andExpect(content()
					.string(containsString("Wrong implementation type " + impl + " specified, retry")));
	}

	@WithMockUser(username = "1111111111", roles = "CLIENT")
	@Test
	void credentials_forbidden() throws Exception {
		
        mockMVC.perform(put("/control").with(csrf()).param("impl", "XXX"))
			.andExpect(status().isForbidden());
	}
	
}
