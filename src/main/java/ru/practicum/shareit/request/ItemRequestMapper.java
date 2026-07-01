package ru.practicum.shareit.request;

import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

public class ItemRequestMapper {

    public static ItemRequest toEntity(ItemRequestDto dto, User user) {
        if (dto == null) {
            return null;
        }

        return ItemRequest.builder()
                .description(dto.getDescription())
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static ItemRequestDto toDto(ItemRequest itemRequest) {
        if (itemRequest == null) {
            return null;
        }

        return ItemRequestDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .requestorId(itemRequest.getUser() != null ? itemRequest.getUser().getId() : null)
                .requestorName(itemRequest.getUser() != null ? itemRequest.getUser().getName() : null)
                .createdAt(itemRequest.getCreatedAt())
                .build();
    }
}