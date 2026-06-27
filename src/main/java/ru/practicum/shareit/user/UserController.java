package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@Valid @RequestBody UserDto userDto) {
        log.info("POST /users - создание пользователя");
        return userService.create(userDto);
    }

    @PatchMapping("/{userId}")
    public UserDto update(
            @PathVariable long userId,
            @RequestBody UpdateUserDto updateUserDto) {
        log.info("PATCH /users/{} - обновление пользователя", userId);
        return userService.update(userId, updateUserDto);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long userId) {
        log.info("DELETE /users/{} - удаление пользователя", userId);
        userService.delete(userId);
    }

    @GetMapping("/{userId}")
    public UserDto get(@PathVariable long userId) {
        log.info("GET /users/{} - получение пользователя", userId);
        return userService.get(userId);
    }

    @GetMapping
    public List<UserDto> getAll() {
        log.info("GET /users - получение всех пользователей");
        return userService.getAll();
    }

    @GetMapping("/search")
    public UserDto getByEmail(@RequestParam String email) {
        log.info("GET /users/search?email={} - поиск пользователя по email", email);
        return userService.getByEmail(email);
    }
}