package com.vena.user_service.config;



import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI userServiceApiDocs(){

        return new OpenAPI()
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("User Service API")
                        .description("API documentation for User Service")
                        .contact(getContact())
                        .license(new io.swagger.v3.oas.models.info.License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org"))
                        .version("1.0.0"));
    }


    private static Contact getContact(){
        Contact contact = new Contact();
        contact.setName("John Doe");
        contact.setEmail("john.doe@example.com");
        return contact;
    }
}
