package de.unibayreuth.bayceer.bayeos.gateway;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.webflow.config.AbstractFlowConfiguration;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.executor.FlowExecutor;
import org.springframework.webflow.mvc.builder.MvcViewFactoryCreator;
import org.springframework.webflow.mvc.servlet.FlowController;
import org.springframework.webflow.mvc.servlet.FlowHandlerAdapter;
import org.springframework.webflow.mvc.servlet.FlowHandlerMapping;
import org.springframework.webflow.security.SecurityFlowExecutionListener;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.view.AjaxThymeleafViewResolver;
import org.thymeleaf.spring4.view.FlowAjaxThymeleafView;


    @Configuration
    @AutoConfigureAfter(WebMvcConfig.class)
    
    public class WebFlowConfig extends AbstractFlowConfiguration {
    	

        @Autowired
        private SpringTemplateEngine templateEngine;
        
        @Bean
        public FlowExecutor flowExecutor() {
            return getFlowExecutorBuilder(flowRegistry())
                    .addFlowExecutionListener(new SecurityFlowExecutionListener(),"*")
                    .build();
        }

        @Bean
        public FlowDefinitionRegistry flowRegistry() {
            return getFlowDefinitionRegistryBuilder(flowBuilderServices())
            		.setBasePath("classpath:templates")
            		.addFlowLocationPattern("/**/*-flow.xml")                   
                    .build();
        }
        @Bean
        public FlowBuilderServices flowBuilderServices() {
            return getFlowBuilderServicesBuilder()
                    .setViewFactoryCreator(mvcViewFactoryCreator())
                    .setValidator(validator())
                    // Add Conversion Service 
                    //.setConversionService(conversionService())
                    .setDevelopmentMode(true)                 
                    .build();
        }
        
        @Bean
        public FlowController flowController() {
            FlowController flowController = new FlowController();
            flowController.setFlowExecutor(flowExecutor());
            return flowController;
        }

        @Bean
        public FlowHandlerMapping flowHandlerMapping() {
            FlowHandlerMapping flowHandlerMapping = new FlowHandlerMapping();
            flowHandlerMapping.setFlowRegistry(flowRegistry());
            flowHandlerMapping.setOrder(-1);
            return flowHandlerMapping;
        }

        @Bean
        public FlowHandlerAdapter flowHandlerAdapter() {
            FlowHandlerAdapter flowHandlerAdapter = new FlowHandlerAdapter();
            flowHandlerAdapter.setFlowExecutor(flowExecutor());
            flowHandlerAdapter.setSaveOutputToFlashScopeOnRedirect(true);
            return flowHandlerAdapter;
        }

        @Bean
        public AjaxThymeleafViewResolver thymeleafViewResolver() {
            AjaxThymeleafViewResolver viewResolver = new AjaxThymeleafViewResolver();
            viewResolver.setViewClass(FlowAjaxThymeleafView.class);
            viewResolver.setTemplateEngine(templateEngine);
            viewResolver.setCharacterEncoding("UTF-8");
            return viewResolver;
        }

        @Bean
        public MvcViewFactoryCreator mvcViewFactoryCreator() {
            List<ViewResolver> viewResolvers = new ArrayList<>();
            viewResolvers.add(thymeleafViewResolver());

            MvcViewFactoryCreator mvcViewFactoryCreator = new MvcViewFactoryCreator();
            mvcViewFactoryCreator.setViewResolvers(viewResolvers);
            mvcViewFactoryCreator.setUseSpringBeanBinding(true);
            return mvcViewFactoryCreator;
        }
        
        @Bean
    	public LocalValidatorFactoryBean validator() {
    		return new LocalValidatorFactoryBean();
    	}

    }
	

