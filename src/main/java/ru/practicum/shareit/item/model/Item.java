package ru.practicum.shareit.item.model;

import lombok.*;

@Data
@EqualsAndHashCode(of = {"id"})
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Item {
    private long id;
    private String name;
    private String description;
    private boolean available;
    private long ownerId;
    private String request;
}
