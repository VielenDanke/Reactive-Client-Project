package kz.danke.reactive.client.project.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        final String baseUrl = "http://localhost:8080";

        return WebClient.create(baseUrl);
    }
}
