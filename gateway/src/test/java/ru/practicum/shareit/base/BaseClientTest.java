package ru.practicum.shareit.base;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.client.BaseClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BaseClientTest {

    @Mock
    private RestTemplate restTemplate;

    private BaseClient baseClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        baseClient = new BaseClient("http://localhost:9090", restTemplate);
    }

    @Test
    void get_ShouldReturnResponse() {
        String expectedResponse = "{\"id\":1,\"name\":\"Test\"}";
        ResponseEntity<String> response = new ResponseEntity<>(expectedResponse, HttpStatus.OK);

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(String.class)))
                .thenReturn(response);

        ResponseEntity<String> result = baseClient.get("/users/1", 1L, String.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(expectedResponse);

        verify(restTemplate, times(1)).exchange(
                eq("http://localhost:9090/users/1"),
                eq(HttpMethod.GET),
                any(),
                eq(String.class)
        );
    }

    @Test
    void post_ShouldReturnResponse() {
        Object requestBody = new Object();
        String expectedResponse = "{\"id\":1,\"name\":\"Created\"}";
        ResponseEntity<String> response = new ResponseEntity<>(expectedResponse, HttpStatus.CREATED);

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(String.class)))
                .thenReturn(response);

        ResponseEntity<String> result = baseClient.post("/users", 1L, requestBody, String.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(expectedResponse);

        verify(restTemplate, times(1)).exchange(
                eq("http://localhost:9090/users"),
                eq(HttpMethod.POST),
                any(),
                eq(String.class)
        );
    }

    @Test
    void patch_ShouldReturnResponse() {
        Object requestBody = new Object();
        String expectedResponse = "{\"id\":1,\"name\":\"Updated\"}";
        ResponseEntity<String> response = new ResponseEntity<>(expectedResponse, HttpStatus.OK);

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(String.class)))
                .thenReturn(response);

        ResponseEntity<String> result = baseClient.patch("/users/1", 1L, requestBody, String.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(expectedResponse);

        verify(restTemplate, times(1)).exchange(
                eq("http://localhost:9090/users/1"),
                eq(HttpMethod.PATCH),
                any(),
                eq(String.class)
        );
    }

    @Test
    void delete_ShouldReturnResponse() {
        ResponseEntity<Void> response = new ResponseEntity<>(HttpStatus.NO_CONTENT);

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(Void.class)))
                .thenReturn(response);

        ResponseEntity<Void> result = baseClient.delete("/users/1", 1L, Void.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        verify(restTemplate, times(1)).exchange(
                eq("http://localhost:9090/users/1"),
                eq(HttpMethod.DELETE),
                any(),
                eq(Void.class)
        );
    }

    @Test
    void get_WithoutUserId_ShouldNotAddHeader() {
        String expectedResponse = "{}";
        ResponseEntity<String> response = new ResponseEntity<>(expectedResponse, HttpStatus.OK);

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(String.class)))
                .thenReturn(response);

        ResponseEntity<String> result = baseClient.get("/users", null, String.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);

        verify(restTemplate, times(1)).exchange(
                eq("http://localhost:9090/users"),
                eq(HttpMethod.GET),
                any(),
                eq(String.class)
        );
    }
}