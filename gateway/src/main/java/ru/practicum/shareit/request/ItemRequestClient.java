package ru.practicum.shareit.request;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.client.BaseClient;

@Service
public class ItemRequestClient extends BaseClient {

    public ItemRequestClient(@Value("${shareit-server.url}") String serverUrl, RestTemplate rest) {
        super(serverUrl, rest);
    }

    public ResponseEntity<Object> create(Long userId, ItemRequestDto itemRequestDto) {
        return post("/requests", userId, itemRequestDto, Object.class);
    }

    public ResponseEntity<Object> update(Long userId, Long requestId, ItemRequestDto itemRequestDto) {
        return patch("/requests/" + requestId, userId, itemRequestDto, Object.class, requestId);
    }

    public ResponseEntity<Object> getAllByUser(Long userId) {
        return get("/requests", userId, Object.class);
    }

    public ResponseEntity<Object> getAllOtherUsers(Long userId) {
        return get("/requests/all", userId, Object.class);
    }

    public ResponseEntity<Object> getById(Long userId, Long requestId) {
        return get("/requests/" + requestId, userId, Object.class, requestId);
    }
}