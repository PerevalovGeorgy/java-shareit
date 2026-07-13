package ru.practicum.shareit.user;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateUserDto {

    private String name;

    @Email(message = "Email должен быть в формате user@yandex.ru")
    private String email;
}
