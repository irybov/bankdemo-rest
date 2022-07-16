package com.github.irybov.bankdemoboot.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
//import org.springframework.stereotype.Component;

//@Component
public class AliasConfiguration implements BeanFactoryPostProcessor {

	@Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        beanFactory.registerAlias("accountService", "accountService");
        beanFactory.registerAlias("billService", "billService");
        beanFactory.registerAlias("operationService", "operationService");
    }
	
}
