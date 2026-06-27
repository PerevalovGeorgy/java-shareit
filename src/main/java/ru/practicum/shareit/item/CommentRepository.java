package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c " +
            "JOIN FETCH c.user " +
            "WHERE c.item.id = :itemId " +
            "ORDER BY c.created DESC")
    List<Comment> findAllByItemId(@Param("itemId") Long itemId);;

    @Query("SELECT c FROM Comment c " +
            "JOIN FETCH c.user " +
            "WHERE c.item.id IN :itemIds " +
            "ORDER BY c.created DESC")
    List<Comment> findAllByItemIds(@Param("itemIds") List<Long> itemIds);

    @Query("SELECT c FROM Comment c WHERE c.item.id = :itemId ORDER BY c.created DESC")
    List<Comment> findAllByItemIdOrderByCreatedDesc(@Param("itemId") Long itemId);

    @Query("SELECT c FROM Comment c " +
            "WHERE c.user.id = :userId " +
            "ORDER BY c.created DESC")
    List<Comment> findAllByUserId(@Param("userId") Long userId);
}