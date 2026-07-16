package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NewItemDto {
    private long id;

    private String name;

    private String description;

    @Builder.Default
    private boolean available = false;

    private long ownerId;

    private String request;
}