package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long>, QuerydslPredicateExecutor<Booking> {


    List<Booking> findByBookerIdOrderByStartDesc(Long userId);

    List<Booking> findByBookerIdAndStatusOrderByStartDesc(Long userId, Status status);

    Optional<Booking> findByIdAndBookerId(Long id, Long userId);

    Optional<Booking> findByIdAndItemOwnerId(Long id, Long ownerId);


    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.owner.id = :ownerId " +
            "ORDER BY b.start DESC")
    List<Booking> findAllByItemOwnerId(@Param("ownerId") Long ownerId);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.owner.id = :ownerId " +
            "AND b.status = :status " +
            "ORDER BY b.start DESC")
    List<Booking> findAllByItemOwnerIdAndStatus(@Param("ownerId") Long ownerId,
                                                @Param("status") Status status);


    @Query("SELECT b FROM Booking b " +
            "WHERE b.booker = :userId " +
            "AND b.start <= :now " +
            "AND b.end >= :now " +
            "ORDER BY b.start DESC")
    List<Booking> findCurrentBookings(@Param("userId") Long userId,
                                      @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.owner.id = :ownerId " +
            "AND b.start <= :now " +
            "AND b.end >= :now " +
            "ORDER BY b.start DESC")
    List<Booking> findCurrentBookingsByOwner(@Param("ownerId") Long ownerId,
                                             @Param("now") LocalDateTime now);


    @Query("SELECT b FROM Booking b " +
            "WHERE b.booker = :userId " +
            "AND b.end < :now " +
            "ORDER BY b.start DESC")
    List<Booking> findPastBookings(@Param("userId") Long userId,
                                   @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.owner.id = :ownerId " +
            "AND b.end < :now " +
            "ORDER BY b.start DESC")
    List<Booking> findPastBookingsByOwner(@Param("ownerId") Long ownerId,
                                          @Param("now") LocalDateTime now);


    @Query("SELECT b FROM Booking b " +
            "WHERE b.booker = :userId " +
            "AND b.start > :now " +
            "ORDER BY b.start DESC")
    List<Booking> findFutureBookings(@Param("userId") Long userId,
                                     @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.owner.id = :ownerId " +
            "AND b.start > :now " +
            "ORDER BY b.start DESC")
    List<Booking> findFutureBookingsByOwner(@Param("ownerId") Long ownerId,
                                            @Param("now") LocalDateTime now);


    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END " +
            "FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.booker.id = :userId " +
            "AND b.status = 'APPROVED' " +
            "AND b.end < :now")
    boolean existsByItemIdAndUserIdAndStatusApprovedAndEndBefore(
            @Param("itemId") Long itemId,
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now);

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END " +
            "FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.status = 'APPROVED' " +
            "AND ((:start BETWEEN b.start AND b.end) " +
            "OR (:end BETWEEN b.start AND b.end) " +
            "OR (b.start BETWEEN :start AND :end))")
    boolean existsOverlappingBooking(@Param("itemId") Long itemId,
                                     @Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end);


    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.status = 'APPROVED' " +
            "AND b.start < :now " +
            "ORDER BY b.start DESC")
    List<Booking> findLastBookingsByItemId(@Param("itemId") Long itemId, @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.status = 'APPROVED' " +
            "AND b.start > :now " +
            "ORDER BY b.start ASC")
    List<Booking> findNextBookingsByItemId(@Param("itemId") Long itemId, @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.status = 'APPROVED' " +
            "ORDER BY b.start DESC")
    List<Booking> findAllApprovedBookingsByItemId(@Param("itemId") Long itemId);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.owner.id = :ownerId " +
            "AND b.status = 'APPROVED' " +
            "ORDER BY b.item.id, b.start DESC")
    List<Booking> findAllApprovedBookingsByOwnerId(@Param("ownerId") Long ownerId);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id IN :itemIds " +
            "AND b.status = 'APPROVED' " +
            "ORDER BY b.item.id, b.start DESC")
    List<Booking> findAllApprovedBookingsByItemIds(@Param("itemIds") List<Long> itemIds);
}