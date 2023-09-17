package com.github.irybov.bankdemoboot.controller;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.github.irybov.bankdemoboot.security.AccountDetailsService;

@WebMvcTest(controllers = MegaController.class)
class MegaControllerTest {

	@MockBean
	private AuthController auth;
	@MockBean
	private AdminController admin;
	@MockBean
	private BankController bank;
	@MockBean
	private AccountDetailsService details;
	@Autowired
	private MockMvc mockMVC;
	
	@WithMockUser(username = "remote", roles = "REMOTE")
	@Test
	void can_change_implementations() throws Exception {

		String impl = "DAO";
		String bean = "AccountServiceDAO";
		doNothing().when(details).setServiceImpl(impl);
		when(auth.setServiceImpl(impl)).thenReturn(bean);
		mockMVC.perform(put("/control").with(csrf()).param("impl", impl))
				.andExpect(status().isOk())
				.andExpect(content()
					.string(containsString("Services impementation has been switched to " + bean)));
		
		impl = "JPA";
		bean = "AccountServicePJA";
		doNothing().when(details).setServiceImpl(impl);
		when(auth.setServiceImpl(impl)).thenReturn(bean);
		mockMVC.perform(put("/control").with(csrf()).param("impl", impl))
				.andExpect(status().isOk())
				.andExpect(content()
					.string(containsString("Services impementation has been switched to " + bean)));
	}
	
	@WithMockUser(username = "remote", roles = "REMOTE")
	@Test
	void wrong_implementation_type() throws Exception {
		
		String impl = "XXX";
		mockMVC.perform(put("/control").with(csrf()).param("impl", impl))
				.andExpect(status().isBadRequest())
				.andExpect(content()
					.string(containsString("Wrong implementation type specified, retry")));
	}

}
