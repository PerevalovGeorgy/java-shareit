package ru.practicum.shareit.request;

import ru.practicum.shareit.user.User;
import ru.practicum.shareit.request.ItemResponseDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

    public static ItemRequestDto toDtoWithItems(ItemRequest itemRequest) {
        if (itemRequest == null) {
            return null;
        }

        ItemRequestDto dto = toDto(itemRequest);

        if (itemRequest.getItems() != null) {
            List<ItemResponseDto> itemDtos = itemRequest.getItems().stream()
                    .map(item -> ItemResponseDto.builder()
                            .id(item.getId())
                            .name(item.getName())
                            .ownerId(item.getOwner().getId())
                            .description(item.getDescription())
                            .available(item.isAvailable())
                            .build())
                    .collect(Collectors.toList());
            dto.setItems(itemDtos);
        }

        return dto;
    }

    // Метод для маппинга списка запросов
    public static List<ItemRequestDto> toDtoWithItemsList(List<ItemRequest> requests) {
        if (requests == null) {
            return null;
        }
        return requests.stream()
                .map(ItemRequestMapper::toDtoWithItems)
                .collect(Collectors.toList());
    }
}