package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dal.mapper.UserMapper;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserDao userDao;
    private final UserMapper mapper;

    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    @Override
    public UserDto get(long userId) {
        log.info("Запрос пользователя с id: {}", userId);
        User existingUser = checkUserExists(userId);

        return mapper.toUserDto(existingUser);
    }

    @Override
    public UserDto create(UserDto userDto) {
        log.info("Создание пользователя: {}", userDto);

        validateEmail(userDto.getEmail());
        checkUniqueEmail(userDto);

        User user = mapper.toEntity(userDto);
        User created = userDao.create(user);
        return mapper.toUserDto(created);
    }

    @Override
    public UserDto update(long userId, UpdateUserDto updateUserDto) {
        log.info("Обновление пользователя: {}", userId);

        User existingUser = checkUserExists(userId);

        if (updateUserDto.getName() != null && !updateUserDto.getName().isBlank()) {
            existingUser.setName(updateUserDto.getName());
        }

        if (updateUserDto.getEmail() != null && !updateUserDto.getEmail().isBlank()) {
            validateEmail(updateUserDto.getEmail());

            if (!updateUserDto.getEmail().equals(existingUser.getEmail())) {
                if (userDao.existsByEmail(updateUserDto.getEmail())) {
                    throw new EmailAlreadyExistsException("Пользователь с email " + updateUserDto.getEmail() + " уже существует");
                }
                existingUser.setEmail(updateUserDto.getEmail());
            }
        }


        User updated = userDao.update(existingUser);
        return mapper.toUserDto(updated);
    }

    @Override
    public void delete(long userId) {
        log.info("Удаление пользователя: {}", userId);
        userDao.delete(userId);
    }

    @Override
    public void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ValidationException("Email не может быть пустым");
        }

        if (!email.matches(EMAIL_PATTERN)) {
            throw new ValidationException("Email должен быть в формате user@example.com");
        }
    }

    private void checkUniqueEmail(UserDto userDto) {
        if (userDao.existsByEmail(userDto.getEmail())) {
            throw new EmailAlreadyExistsException("Пользователь с email " + userDto.getEmail() + " уже существует");
        }
    }

    public  User checkUserExists(long userId) {
        Optional<User> userOptional = userDao.findById(userId);

        if (userOptional.isEmpty()) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        return userOptional.get();
    }
}
