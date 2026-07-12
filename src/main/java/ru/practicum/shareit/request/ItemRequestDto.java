package ru.practicum.shareit.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemRequestDto {
    private Long id;

    private String description;

    private Long requestorId;

    private LocalDateTime createdAt;

    private String requestorName;
}
