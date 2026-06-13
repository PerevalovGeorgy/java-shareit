package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;

public interface UserService {
    UserDto get(long userId);
    UserDto create(UserDto userDto);
    UserDto update(long userId, UpdateUserDto updateUserDto);
    void delete(long userId);
    void validateEmail(String email);
}
