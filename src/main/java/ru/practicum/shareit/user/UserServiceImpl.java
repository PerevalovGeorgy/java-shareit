package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    @Override
    public UserDto get(long userId) {
        log.info("Запрос пользователя с id: {}", userId);
        User existingUser = checkUserExists(userId);

        return UserMapper.toUserDto(existingUser);
    }

    @Override
    public UserDto getByEmail(String email) {
        log.info("Поиск пользователя по email: {}", email);

        validateEmail(email);

        User user = userRepository.findUserByEmailIgnoreCase(email)
                .orElseThrow(() -> new NotFoundException("Пользователь с email " + email + " не найден"));

        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAll() {
        log.info("Получение всех пользователей");
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDto create(UserDto userDto) {
        log.info("Создание пользователя: {}", userDto);

        validateEmail(userDto.getEmail());
        checkUniqueEmail(userDto.getEmail());

        User user = UserMapper.toEntity(userDto);
        User created = userRepository.save(user);
        log.info("Пользователь создан с id: {}", created.getId());
        return UserMapper.toUserDto(created);
    }

    @Override
    @Transactional
    public UserDto update(long userId, UpdateUserDto updateUserDto) {
        log.info("Обновление пользователя: {}", userId);

        User existingUser = checkUserExists(userId);

        if (updateUserDto.getName() != null && !updateUserDto.getName().isBlank()) {
            existingUser.setName(updateUserDto.getName());
        }

        if (updateUserDto.getEmail() != null && !updateUserDto.getEmail().isBlank()) {
            validateEmail(updateUserDto.getEmail());

            if (!updateUserDto.getEmail().equalsIgnoreCase(existingUser.getEmail())) {
                checkUniqueEmail(updateUserDto.getEmail());
                existingUser.setEmail(updateUserDto.getEmail());
            }
        }

        User updated =  userRepository.save(existingUser);
        log.info("Пользователь с id {} обновлен", userId);
        return UserMapper.toUserDto(updated);
    }

    @Override
    @Transactional
    public void delete(long userId) {
        log.info("Удаление пользователя: {}", userId);
        User user = checkUserExists(userId);
        userRepository.delete(user);
        log.info("Пользователь с id = {} удален", userId);
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

    private void checkUniqueEmail(String email) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyExistsException("Пользователь с email " + email + " уже существует");
        }
    }

    public User checkUserExists(long userId) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        return userOptional.get();
    }
}
