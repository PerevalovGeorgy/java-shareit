package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long>, QuerydslPredicateExecutor<Item> {
    List<Item> findByOwnerId(Long ownerId);

    @Query("SELECT i FROM Item i " +
            "WHERE i.available = true " +
            "AND (LOWER(i.name) LIKE LOWER(CONCAT('%', ?1, '%')) " +
            "OR LOWER(i.description) LIKE LOWER(CONCAT('%', ?1, '%')))")
    List<Item> searchAvailable(@Param("text") String text);

    boolean existsByIdAndOwnerId(Long id, Long ownerId);

    @Query("SELECT i FROM Item i " +
            "JOIN FETCH i.owner " +
            "WHERE i.id = :itemId")
    Optional<Item> findByIdWithOwner(@Param("itemId") Long itemId);

    @Query("SELECT i FROM Item i " +
            "JOIN FETCH i.owner " +
            "WHERE i.owner.id = :ownerId")
    List<Item> findByOwnerIdWithOwner(@Param("ownerId") Long ownerId);
}
