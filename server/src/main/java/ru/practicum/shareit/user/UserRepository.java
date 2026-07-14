package ru.practicum.shareit.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, QuerydslPredicateExecutor<User> {

    Optional<User> findUserByEmailIgnoreCase(String emailPart);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
