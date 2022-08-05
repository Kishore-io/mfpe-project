package com.cts.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
// class for swagger

@Configuration
@EnableSwagger2

public class SwaggerConfig {
	
	/** return Docket */
	
	 @Bean
	 public Docket postsApi() {
         return new Docket(DocumentationType.SWAGGER_2)
                 .apiInfo(apiInfo())
                 .select()
                 .build();
     }
	 
	 /** return Apiinfo */
	 
	 private ApiInfo apiInfo() {
         return new ApiInfoBuilder()
                 .title("Refill Service")
                 .description("Refill API for Mail-order-Pharmacy")
                 .termsOfServiceUrl("http://www.cognizant.com")
                 .version("2.0")
                 .build();
     }

}
