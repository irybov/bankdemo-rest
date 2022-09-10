package com.github.irybov.bankdemoboot.config;

import javax.sql.DataSource;

//import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.github.irybov.bankdemoboot.service.AccountService;
import com.github.irybov.bankdemoboot.service.BillService;
import com.github.irybov.bankdemoboot.service.OperationService;
import com.github.irybov.bankdemoboot.validation.AccountValidator;

@Configuration
@ComponentScan(basePackages="bankdemoboot")
public class AppConfig {

    @Autowired
    private ApplicationContext context;
	
	@ConfigurationProperties(prefix = "spring.datasource")
	@Bean
	@Primary
	public DataSource datasource() {
		return DataSourceBuilder.create().build();
	}
	
    @Bean
    public AccountService accountServiceAlias(@Value("${bean.service-impl.account}")
    String qualifier) {
        return (AccountService) context.getBean(qualifier);
    }
    @Bean
    public BillService billServiceAlias(@Value("${bean.service-impl.bill}")
    String qualifier) {
        return (BillService) context.getBean(qualifier);
    }
    @Bean
    public OperationService operationServiceAlias(@Value("${bean.service-impl.operation}")
    String qualifier) {
        return (OperationService) context.getBean(qualifier);
    }    
    
    @Bean
    @Primary
    public AccountValidator beforeCreateAccountValidator() {
        return new AccountValidator();
    }
    
/*    @Bean
    public ModelMapper modelmMapper() {
    	return new ModelMapper();
    }*/
    
}
