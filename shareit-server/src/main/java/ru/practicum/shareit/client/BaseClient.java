package ru.practicum.shareit.client;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

public class BaseClient {
    protected final RestTemplate rest;
    protected final String serverUrl;

    public BaseClient(String serverUrl, RestTemplate rest) {
        this.serverUrl = serverUrl;
        this.rest = rest;
    }

    protected <T> ResponseEntity<T> get(String path, Class<T> responseType, Object... params) {
        return makeRequest(HttpMethod.GET, path, null, responseType, params);
    }

    protected <T> ResponseEntity<T> post(String path, Object body, Class<T> responseType, Object... params) {
        return makeRequest(HttpMethod.POST, path, body, responseType, params);
    }

    protected <T> ResponseEntity<T> patch(String path, Object body, Class<T> responseType, Object... params) {
        return makeRequest(HttpMethod.PATCH, path, body, responseType, params);
    }

    protected <T> ResponseEntity<T> delete(String path, Class<T> responseType, Object... params) {
        return makeRequest(HttpMethod.DELETE, path, null, responseType, params);
    }

    protected <T> ResponseEntity<T> makeRequest(HttpMethod method, String path, Object body,
                                                Class<T> responseType, Object... params) {
        String url = serverUrl + String.format(path, params);
        HttpEntity<Object> entity = new HttpEntity<>(body, new HttpHeaders());

        ResponseEntity<T> response = rest.exchange(url, method, entity, responseType);
        return response;
    }
}