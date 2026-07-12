package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserClient userClient;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object>  create(@Valid @RequestBody UserDto userDto) {
        log.info("POST /users - создание пользователя");
        return userClient.create(userDto);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<Object> update(
            @PathVariable long userId,
            @Valid @RequestBody UpdateUserDto updateUserDto) {
        log.info("PATCH /users/{} - обновление пользователя", userId);
        return userClient.update(userId, updateUserDto);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long userId) {
        log.info("DELETE /users/{} - удаление пользователя", userId);
        userClient.delete(userId);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object>  get(@PathVariable long userId) {
        log.info("GET /users/{} - получение пользователя", userId);
        return userClient.get(userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAll() {
        log.info("GET /users - получение всех пользователей");
        return userClient.getAll();
    }

    @GetMapping("/search")
    public ResponseEntity<Object>  getByEmail(@RequestParam String email) {
        log.info("GET /users/search?email={} - поиск пользователя по email", email);
        return userClient.getByEmail(email);
    }
}