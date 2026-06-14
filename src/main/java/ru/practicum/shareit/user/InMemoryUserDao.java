package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Repository
public class InMemoryUserDao implements UserDao {

    private final Map<Long, User> users = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public boolean existsById(long id) {
        log.info("Проверка существования пользователя с id: {}", id);
        return users.containsKey(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        log.info("Проверка существования email: {}", email);
        return users.values().stream()
                .anyMatch(user -> user.getEmail().equals(email));
    }

    @Override
    public Optional<User> findById(long id) {
        log.info("Поиск пользователя с id: {}", id);
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public User create(User user) {
        log.info("Создание нового пользователя: {}", user);
        long newId = idGenerator.getAndIncrement();
        user.setId(newId);
        users.put(newId, user);
        return user;
    }

    @Override
    public User update(User user) {
        log.info("Изменение пользователя: {}", user);

        users.put(user.getId(), user);
        return user;

    }

    @Override
    public void delete(long userId) {
        log.info("Удаление пользователя с id: {}", userId);
        users.remove(userId);
    }
}