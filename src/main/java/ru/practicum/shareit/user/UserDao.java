package ru.practicum.shareit.user;


import java.util.Optional;

public interface UserDao {
    boolean existsById(long id);
    User create(User user);
    User update(User user);
    void delete(long userId);
    boolean existsByEmail(String email);
    Optional<User> findById(long id);
}
