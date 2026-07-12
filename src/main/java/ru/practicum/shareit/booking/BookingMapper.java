package ru.practicum.shareit.booking;

import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;

public class BookingMapper {

    public static Booking toEntity(BookingRequestDto bookingRequestDto, Item item, User booker) {
        if (bookingRequestDto == null) {
            return null;
        }

        return Booking.builder()
                .start(bookingRequestDto.getStart())
                .end(bookingRequestDto.getEnd())
                .item(item)
                .booker(booker)
                .status(Status.WAITING)
                .build();
    }

    public static BookingResponseDto toResponseDto(Booking booking) {
        if (booking == null) {
            return null;
        }

        return BookingResponseDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .booker(UserMapper.toUserDto(booking.getBooker()))
                .item(ItemMapper.toItemDto(booking.getItem()))
                .build();
    }


    public static BookingRequestDto toRequestDto(Booking booking) {
        if (booking == null) {
            return null;
        }

        return BookingRequestDto.builder()
                .start(booking.getStart())
                .end(booking.getEnd())
                .itemId(booking.getItem().getId())
                .build();
    }

    public static BookingShortDto toShortDto(Booking booking) {
        if (booking == null) {
            return null;
        }

        return BookingShortDto.builder()
                .id(booking.getId())
                .bookerId(booking.getBooker().getId())
                .bookerName(booking.getBooker().getName())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .build();
    }

}
