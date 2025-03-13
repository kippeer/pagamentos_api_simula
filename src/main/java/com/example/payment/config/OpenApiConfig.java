package com.example.payment.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI paymentSystemOpenAPI() {
        Server devServer = new Server()
            .url("http://localhost:8080")
            .description("Development server");
            
        Contact contact = new Contact()
            .name("Payment System Team")
            .email("support@paymentsystem.com")
            .url("https://paymentsystem.com");
            
        License license = new License()
            .name("MIT License")
            .url("https://opensource.org/licenses/MIT");
            
        Info info = new Info()
            .title("Payment System API")
            .version("1.0.0")
            .description("API for processing payments using PIX, Credit Card, and QR Code")
            .contact(contact)
            .license(license);
            
        return new OpenAPI()
            .info(info)
            .servers(List.of(devServer));
    }
}