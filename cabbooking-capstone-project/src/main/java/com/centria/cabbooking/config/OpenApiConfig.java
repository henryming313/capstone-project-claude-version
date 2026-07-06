package com.centria.cabbooking.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI cabBookingOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Cab Booking System API")
                .version("v1")
                .description("Capstone project — Centria University of Applied Sciences. " +
                        "Role-based ride-hailing MVP for Central Ostrobothnia, Finland.")
                .contact(new Contact().name("Centria Capstone Team")));
    }
}
