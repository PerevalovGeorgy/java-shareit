package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
@Validated
public class ItemRequestController {

    private final ItemRequestClient itemRequestClient;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> create(
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId,
            @Valid @RequestBody ItemRequestDto itemRequestDto) {
        log.info("POST /requests - создание запроса пользователем {}", userId);
        return itemRequestClient.create(userId, itemRequestDto);
    }

    @PatchMapping("/{requestId}")
    public ResponseEntity<Object> update(
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId,
            @PathVariable @Positive Long requestId,
            @Valid @RequestBody ItemRequestDto itemRequestDto) {
        log.info("PATCH /requests/{} - обновление запроса пользователем {}", requestId, userId);
        return itemRequestClient.update(userId, requestId, itemRequestDto);
    }

    @GetMapping
    public ResponseEntity<Object> getAllByUser(
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        log.info("GET /requests - получение всех запросов пользователя {}", userId);
        return itemRequestClient.getAllByUser(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllOtherUsers(
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        log.info("GET /requests/all - получение всех запросов других пользователей, кроме {}", userId);
        return itemRequestClient.getAllOtherUsers(userId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getById(
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId,
            @PathVariable @Positive Long requestId) {
        log.info("GET /requests/{} - получение запроса пользователем {}", requestId, userId);
        return itemRequestClient.getById(userId, requestId);
    }
}