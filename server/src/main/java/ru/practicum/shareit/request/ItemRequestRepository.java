package ru.practicum.shareit.request;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long>,
        QuerydslPredicateExecutor<ItemRequest> {

    @EntityGraph(attributePaths = {"items", "items.owner"})
    @Query("SELECT r FROM ItemRequest r WHERE r.user.id = :userId ORDER BY r.created DESC")
    List<ItemRequest> findAllByUserIdOrderByCreatedDesc(@Param("userId") Long userId);

    @EntityGraph(attributePaths = {"items", "items.owner"})
    @Query("SELECT r FROM ItemRequest r WHERE r.user.id != :userId ORDER BY r.created DESC")
    List<ItemRequest> findAllByUserIdNotOrderByCreatedDesc(@Param("userId") Long userId);

    @EntityGraph(attributePaths = {"items", "items.owner"})
    @Query("SELECT r FROM ItemRequest r ORDER BY r.created DESC")
    List<ItemRequest> findAllOrderByCreatedDesc();

    @EntityGraph(attributePaths = {"items", "items.owner"})
    Optional<ItemRequest> findById(Long id);
}