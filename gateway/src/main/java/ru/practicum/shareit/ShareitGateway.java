package ru.practicum.shareit;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@SpringBootApplication
public class ShareitGateway {
    public static void main(String[] args) {
        SpringApplication.run(ShareitGateway.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        HttpClient httpClient = HttpClients.createDefault();
        HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory(httpClient);
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setConnectionRequestTimeout(Duration.ofSeconds(5));
        return new RestTemplate(factory);
    }
}