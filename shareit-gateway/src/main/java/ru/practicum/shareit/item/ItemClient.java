package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.client.BaseClient;

@Service
public class ItemClient extends BaseClient {

    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplate rest) {
        super(serverUrl, rest);
    }

    public ResponseEntity<Object> create(long userId, ItemDto itemDto) {
        return post("/items", userId, itemDto, Object.class);
    }

    public ResponseEntity<Object> update(long userId, long itemId, ItemDto itemDto) {
        return patch("/items/" + itemId, userId, itemDto, Object.class, itemId);
    }

    public ResponseEntity<Object> findById(long userId, long itemId) {
        return get("/items/" + itemId, userId, Object.class, itemId);
    }

    public ResponseEntity<Object> findAllByOwner(long userId) {
        return get("/items", userId, Object.class);
    }

    public ResponseEntity<Object> search(String text) {
        return get("/items/search?text={text}", null, Object.class, text);
    }

    public ResponseEntity<Object> delete(long userId, long itemId) {
        return delete("/items/" + itemId, userId, Object.class, itemId);
    }

    public ResponseEntity<Object> addComment(long userId, long itemId, CommentTextDto commentTextDto) {
        return post("/items/" + itemId + "/comment", userId, commentTextDto, Object.class, itemId);
    }
}