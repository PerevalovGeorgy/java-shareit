package ru.practicum.shareit.booking;

import lombok.*;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(of = {"id"})
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Booking {
   private long id;
   private LocalDateTime start;
   private LocalDateTime end;
   private long itemId;
   private long bookerId;
   private Status status;
}
