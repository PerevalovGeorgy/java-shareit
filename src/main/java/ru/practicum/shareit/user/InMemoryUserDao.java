package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Repository
public class InMemoryUserDao implements UserDao {

    private final Map<Long, User> users = new HashMap<>();

    @Override
    public boolean existsById(long id) {
        return users.containsKey(id);
    }
}