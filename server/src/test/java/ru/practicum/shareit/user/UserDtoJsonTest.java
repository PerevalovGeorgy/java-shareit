package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@ContextConfiguration(classes = {UserDto.class, UpdateUserDto.class})
class UserDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void serializeUserDto_ShouldReturnValidJson() throws Exception {
        UserDto userDto = UserDto.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .build();

        String json = objectMapper.writeValueAsString(userDto);

        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"name\":\"Test User\"");
        assertThat(json).contains("\"email\":\"test@example.com\"");
    }

    @Test
    void deserializeUserDto_ShouldReturnValidObject() throws Exception {
        String json = "{\"id\":1,\"name\":\"Test User\",\"email\":\"test@example.com\"}";

        UserDto userDto = objectMapper.readValue(json, UserDto.class);

        assertThat(userDto.getId()).isEqualTo(1L);
        assertThat(userDto.getName()).isEqualTo("Test User");
        assertThat(userDto.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void serializeUpdateUserDto_ShouldReturnValidJson() throws Exception {
        UpdateUserDto updateDto = UpdateUserDto.builder()
                .name("Updated User")
                .email("updated@example.com")
                .build();

        String json = objectMapper.writeValueAsString(updateDto);

        assertThat(json).contains("\"name\":\"Updated User\"");
        assertThat(json).contains("\"email\":\"updated@example.com\"");
    }

    @Test
    void deserializeUpdateUserDto_ShouldReturnValidObject() throws Exception {
        String json = "{\"name\":\"Updated User\",\"email\":\"updated@example.com\"}";

        UpdateUserDto updateDto = objectMapper.readValue(json, UpdateUserDto.class);

        assertThat(updateDto.getName()).isEqualTo("Updated User");
        assertThat(updateDto.getEmail()).isEqualTo("updated@example.com");
    }

    @Test
    void serializeUserDto_WithNullFields_ShouldReturnValidJson() throws Exception {
        UserDto userDto = UserDto.builder().build();

        String json = objectMapper.writeValueAsString(userDto);

        assertThat(json).contains("\"id\":null");
        assertThat(json).contains("\"name\":null");
        assertThat(json).contains("\"email\":null");
    }
}