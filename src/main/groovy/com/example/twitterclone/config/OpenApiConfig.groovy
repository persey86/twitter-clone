package com.example.twitterclone.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    OpenAPI customOpenApi() {
        new OpenAPI()
                .info(new Info()
                        .title("Groovy Switter API")
                        .version("1.0")
                        .description("API for Twitter clone on Groovy"))
    }
}
