package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ItemRequestMapper {

    private final UserMapper userMapper;

    public ItemRequest toEntity(ItemRequestDto dto, User user) {
        if (dto == null) {
            return null;
        }

        return ItemRequest.builder()
                .description(dto.getDescription())
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public ItemRequestDto toDto(ItemRequest itemRequest) {
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