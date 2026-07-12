package ru.practicum.shareit.booking;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingRequestDto {

    @NotNull(message = "Время начала бронирования не может быть пустым")
    @FutureOrPresent(message = "Время начала должно быть в настоящем или будущем")
    private LocalDateTime start;

    @NotNull(message = "Время окончания бронирования не может быть пустым")
    @Future(message = "Время окончания должно быть в будущем")
    private LocalDateTime end;

    @NotNull(message = "ID вещи не может быть пустым")
    private Long itemId;
}
