package ru.practicum.shareit.user;

import java.util.List;

public interface UserService {
    UserDto get(long userId);

    UserDto getByEmail(String email);

    List<UserDto> getAll();

    UserDto create(UserDto userDto);

    UserDto update(long userId, UpdateUserDto updateUserDto);

    void delete(long userId);

    void validateEmail(String email);
}
