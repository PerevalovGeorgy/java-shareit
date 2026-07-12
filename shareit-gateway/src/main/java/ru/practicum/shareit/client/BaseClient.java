package ru.practicum.shareit.client;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

public class BaseClient {
    protected final RestTemplate rest;
    protected final String serverUrl;

    public BaseClient(String serverUrl, RestTemplate rest) {
        this.serverUrl = serverUrl;
        this.rest = rest;
    }

    protected <T> ResponseEntity<T> get(String path, Long userId, Class<T> responseType, Object... params) {
        return makeRequest(HttpMethod.GET, path, userId, null, responseType, params);
    }

    protected <T> ResponseEntity<T> post(String path, Long userId, Object body, Class<T> responseType, Object... params) {
        return makeRequest(HttpMethod.POST, path, userId, body, responseType, params);
    }

    protected <T> ResponseEntity<T> patch(String path, Long userId, Object body, Class<T> responseType, Object... params) {
        return makeRequest(HttpMethod.PATCH, path, userId, body, responseType, params);
    }

    protected <T> ResponseEntity<T> delete(String path, Long userId, Class<T> responseType, Object... params) {
        return makeRequest(HttpMethod.DELETE, path, userId, null, responseType, params);
    }

    private <T> ResponseEntity<T> makeRequest(HttpMethod method, String path, Long userId,
                                              Object body, Class<T> responseType, Object... params) {
        String url = serverUrl + String.format(path, params);

        HttpHeaders headers = new HttpHeaders();
        if (userId != null) {
            headers.set("X-Sharer-User-Id", String.valueOf(userId));
        }

        HttpEntity<Object> entity = new HttpEntity<>(body, headers);

        return rest.exchange(url, method, entity, responseType);
    }
}