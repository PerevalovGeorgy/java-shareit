package ru.practicum.shareit.booking;

import ru.practicum.shareit.exception.BadRequestException;

public enum Status {
    WAITING,
    APPROVED,
    REJECTED,
    CANCELED,
    ALL;



    public static Status parse(String state) {
        if (state == null || state.isBlank()) {
            return ALL;
        }
        try {
            return Status.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Unknown state: " + state);
        }
    }
}
