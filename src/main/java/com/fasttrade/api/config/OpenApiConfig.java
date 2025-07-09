package com.fasttrade.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI fastTradeOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FastTrade API")
                        .version("1.0")
                        .description("📱 API REST para intermediação de trocas monetárias entre usuários em diferentes moedas. Ideal para quem quer fazer o câmbio de forma rápida, segura e com baixíssima taxa. 💸")
                );
    }
}
