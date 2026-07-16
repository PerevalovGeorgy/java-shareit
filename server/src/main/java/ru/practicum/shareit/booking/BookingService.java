package ru.practicum.shareit.booking;

import java.util.List;

public interface BookingService {

    BookingResponseDto create(Long userId, BookingRequestDto bookingRequestDto);

    BookingResponseDto update(Long userId, Long bookingId, Boolean approved);

    BookingResponseDto get(Long userId, Long bookingId);

    List<BookingResponseDto> getAllByUser(Long userId, String state);

    List<BookingResponseDto> getAllByOwner(Long userId, String state);
}
