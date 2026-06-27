package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;


@RestController
@RequestMapping("/items")
@Slf4j
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;
    private static final String REQUESTHEADER = "X-Sharer-User-Id";

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto create(
            @RequestHeader(REQUESTHEADER) long userId,
            @Valid @RequestBody ItemDto itemDto) {
        log.info("POST /items - создание вещи, userId={}", userId);
        return itemService.create(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(
            @RequestHeader(REQUESTHEADER) long userId,
            @PathVariable long itemId,
            @RequestBody ItemDto itemDto) {
        log.info("PATCH /items/{} - обновление вещи, userId={}", itemId, userId);
        return itemService.update(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemDto findById(
            @RequestHeader(REQUESTHEADER) long userId,
            @PathVariable long itemId) {
        log.info("GET /items/{} - получение вещи, userId={}", itemId, userId);
        return itemService.findById(userId, itemId);
    }

    @GetMapping
    public Collection<ItemDto> findAllByOwner(
            @RequestHeader(REQUESTHEADER) long userId) {
        log.info("GET /items - получение всех вещей пользователя, userId={}", userId);
        return itemService.findAllByOwner(userId);
    }

    @GetMapping("/search")
    public Collection<ItemDto> search(@RequestParam String text) {
        log.info("GET /items/search - поиск вещей, text={}", text);
        return itemService.searchAvailable(text);
    }

    @DeleteMapping("/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @RequestHeader(REQUESTHEADER) long userId,
            @PathVariable long itemId) {
        log.info("DELETE /items/{} - удаление вещи, userId={}", itemId, userId);
        itemService.delete(userId, itemId);
    }

    @PostMapping("/{itemId}/comment")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addComment(
            @RequestHeader(REQUESTHEADER) long userId,
            @PathVariable long itemId,
            @Valid @RequestBody CommentDto commentDto) {
        log.info("POST /items/{}/comment - добавление комментария к вещи, userId={}", itemId, userId);
        return itemService.addComment(userId, itemId, commentDto);
    }
}