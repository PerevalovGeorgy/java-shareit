package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/items")
@Slf4j
@RequiredArgsConstructor
public class ItemController {
    private final ItemClient itemClient;
    private static final String REQUESTHEADER = "X-Sharer-User-Id";

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> create(
            @RequestHeader(REQUESTHEADER) long userId,
            @Valid @RequestBody ItemDto itemDto) {
        log.info("POST /items - создание вещи, userId={}", userId);
        return itemClient.create(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(
            @RequestHeader(REQUESTHEADER) long userId,
            @PathVariable long itemId,
            @RequestBody ItemDto itemDto) {
        log.info("PATCH /items/{} - обновление вещи, userId={}", itemId, userId);
        return itemClient.update(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> findById(
            @RequestHeader(REQUESTHEADER) long userId,
            @PathVariable long itemId) {
        log.info("GET /items/{} - получение вещи, userId={}", itemId, userId);
        return itemClient.findById(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> findAllByOwner(
            @RequestHeader(REQUESTHEADER) long userId) {
        log.info("GET /items - получение всех вещей пользователя, userId={}", userId);
        return itemClient.findAllByOwner(userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestParam String text) {
        log.info("GET /items/search - поиск вещей, text={}", text);
        return itemClient.search(text);
    }

    @DeleteMapping("/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @RequestHeader(REQUESTHEADER) long userId,
            @PathVariable long itemId) {
        log.info("DELETE /items/{} - удаление вещи, userId={}", itemId, userId);
        itemClient.delete(userId, itemId);
    }

    @PostMapping("/{itemId}/comment")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> addComment(
            @RequestHeader(REQUESTHEADER) long userId,
            @PathVariable long itemId,
            @Valid @RequestBody CommentTextDto commentTextDto) {
        log.info("POST /items/{}/comment - добавление комментария к вещи, userId={}", itemId, userId);
        return itemClient.addComment(userId, itemId, commentTextDto);
    }
}