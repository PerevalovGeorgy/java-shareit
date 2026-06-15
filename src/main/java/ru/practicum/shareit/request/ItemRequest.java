package ru.practicum.shareit.request;

import lombok.*;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(of = {"id"})
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemRequest {
    private long id;
    private String description;
    private long requestorId;
    private LocalDateTime created;
}
