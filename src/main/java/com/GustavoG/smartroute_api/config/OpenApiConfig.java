package com.GustavoG.smartroute_api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SmartRoute API")
                        .description("API de Roteirização Inteligente para entregas do Mercado Livre")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Gustavo")
                                .email("gustavo.germanolemos@gmail.com")
                        ));
    }
}