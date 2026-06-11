package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NewItemDto {
    private long id;

    @NotBlank(message = "Название вещи не может быть пустым")
    private String name;

    private String description;

    @NotNull(message = "Занятость вещи должена быть указана")
    @Builder.Default
    private boolean available = false;

    @NotBlank(message = "Владелец вещи должен быть указан")
    private long ownerId;

    private String request;
}