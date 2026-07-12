package ru.practicum.shareit.request;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long>,
        QuerydslPredicateExecutor<ItemRequest> {

    @Query("SELECT r FROM ItemRequest r WHERE r.user.id = :userId ORDER BY r.createdAt DESC")
    List<ItemRequest> findAllByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    @Query("SELECT r FROM ItemRequest r WHERE r.user.id != :userId ORDER BY r.createdAt DESC")
    List<ItemRequest> findAllByUserIdNotOrderByCreatedAtDesc(@Param("userId") Long userId);

    @Query("SELECT r FROM ItemRequest r ORDER BY r.createdAt DESC")
    List<ItemRequest> findAllOrderByCreatedAtDesc();
}