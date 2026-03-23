package com.study.paymentservice.client;

import com.study.paymentservice.exception.RandomNumberClientException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class RandomNumberClient {
    private static final String API_URL = "https://www.randomnumberapi.com/api/v1.0/random?min=1&max=100&count=1";
    private final RestTemplate restTemplate = new RestTemplate();

    public int getRandomNumber() {
        int[] response = new int[1];
        try {
            response = restTemplate.getForObject(API_URL, int[].class);
        } catch (RestClientException ex) {
            throw new RandomNumberClientException("Random number API error:", ex.getCause());
        }
        return response[0];
    }
}
