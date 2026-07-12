package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.client.BaseClient;

@Service
public class UserClient extends BaseClient {

    public UserClient(@Value("${shareit-server.url}") String serverUrl, RestTemplate rest) {
        super(serverUrl, rest);
    }

    public ResponseEntity<Object> create(UserDto userDto) {
        return post("/users", null, userDto, Object.class);  // userId = null для создания
    }

    public ResponseEntity<Object> update(long userId, UpdateUserDto updateUserDto) {
        return patch("/users/{userId}", userId, updateUserDto, Object.class, userId);
    }

    public ResponseEntity<Object> delete(long userId) {
        return delete("/users/{userId}", userId, Object.class, userId);
    }

    public ResponseEntity<Object> get(long userId) {
        return get("/users/{userId}", userId, Object.class, userId);
    }

    public ResponseEntity<Object> getAll() {
        return get("/users", null, Object.class);  // userId = null
    }

    public ResponseEntity<Object> getByEmail(String email) {
        return get("/users/search?email={email}", null, Object.class, email);
    }
}