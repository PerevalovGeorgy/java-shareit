package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.test.context.ContextConfiguration;
import ru.practicum.shareit.request.*;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@ContextConfiguration(classes = {ItemRequestDto.class, ItemResponseDto.class})
class ItemRequestDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void serializeItemRequestDto_ShouldReturnValidJson() throws Exception {
        ItemResponseDto itemResponse = ItemResponseDto.builder()
                .id(1L)
                .name("Drill")
                .ownerId(2L)
                .description("Power drill")
                .available(true)
                .build();

        ItemRequestDto requestDto = ItemRequestDto.builder()
                .id(1L)
                .description("Need a drill")
                .requestorId(1L)
                .requestorName("User1")
                .created(LocalDateTime.of(2026, 7, 13, 10, 0, 0))
                .items(Arrays.asList(itemResponse))
                .build();

        String json = objectMapper.writeValueAsString(requestDto);

        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"description\":\"Need a drill\"");
        assertThat(json).contains("\"requestorId\":1");
        assertThat(json).contains("\"requestorName\":\"User1\"");
        assertThat(json).contains("\"created\":\"2026-07-13T10:00:00\"");
        assertThat(json).contains("\"items\"");
        assertThat(json).contains("\"name\":\"Drill\"");
    }

    @Test
    void deserializeItemRequestDto_ShouldReturnValidObject() throws Exception {
        String json = "{\"id\":1,\"description\":\"Need a drill\",\"requestorId\":1,\"requestorName\":\"User1\"}";

        ItemRequestDto requestDto = objectMapper.readValue(json, ItemRequestDto.class);

        assertThat(requestDto.getId()).isEqualTo(1L);
        assertThat(requestDto.getDescription()).isEqualTo("Need a drill");
        assertThat(requestDto.getRequestorId()).isEqualTo(1L);
        assertThat(requestDto.getRequestorName()).isEqualTo("User1");
    }

    @Test
    void deserializeItemRequestDto_WithItems_ShouldReturnValidObject() throws Exception {
        String json = "{\"id\":1,\"description\":\"Need a drill\",\"requestorId\":1,\"items\":[{\"id\":1,\"name\":\"Drill\",\"ownerId\":2,\"description\":\"Power drill\",\"available\":true}]}";

        ItemRequestDto requestDto = objectMapper.readValue(json, ItemRequestDto.class);

        assertThat(requestDto.getId()).isEqualTo(1L);
        assertThat(requestDto.getDescription()).isEqualTo("Need a drill");
        assertThat(requestDto.getRequestorId()).isEqualTo(1L);
        assertThat(requestDto.getItems()).isNotEmpty();
        assertThat(requestDto.getItems().get(0).getName()).isEqualTo("Drill");
    }

    @Test
    void serializeItemResponseDto_ShouldReturnValidJson() throws Exception {
        ItemResponseDto itemResponse = ItemResponseDto.builder()
                .id(1L)
                .name("Drill")
                .ownerId(2L)
                .description("Power drill")
                .available(true)
                .build();

        String json = objectMapper.writeValueAsString(itemResponse);

        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"name\":\"Drill\"");
        assertThat(json).contains("\"ownerId\":2");
        assertThat(json).contains("\"description\":\"Power drill\"");
        assertThat(json).contains("\"available\":true");
    }

    @Test
    void deserializeItemResponseDto_ShouldReturnValidObject() throws Exception {
        String json = "{\"id\":1,\"name\":\"Drill\",\"ownerId\":2,\"description\":\"Power drill\",\"available\":true}";

        ItemResponseDto itemResponse = objectMapper.readValue(json, ItemResponseDto.class);

        assertThat(itemResponse.getId()).isEqualTo(1L);
        assertThat(itemResponse.getName()).isEqualTo("Drill");
        assertThat(itemResponse.getOwnerId()).isEqualTo(2L);
        assertThat(itemResponse.getDescription()).isEqualTo("Power drill");
        assertThat(itemResponse.getAvailable()).isTrue();
    }

    @Test
    void serializeItemRequestDto_WithEmptyItems_ShouldReturnValidJson() throws Exception {
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .id(1L)
                .description("Need a drill")
                .requestorId(1L)
                .items(Arrays.asList())
                .build();

        String json = objectMapper.writeValueAsString(requestDto);

        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"description\":\"Need a drill\"");
        assertThat(json).contains("\"items\":[]");
    }

    @Test
    void deserializeItemResponseDto_WithNullAvailable_ShouldReturnNull() throws Exception {
        String json = "{\"id\":1,\"name\":\"Drill\",\"ownerId\":2,\"description\":\"Power drill\",\"available\":null}";

        ItemResponseDto itemResponse = objectMapper.readValue(json, ItemResponseDto.class);

        assertThat(itemResponse.getId()).isEqualTo(1L);
        assertThat(itemResponse.getName()).isEqualTo("Drill");
        assertThat(itemResponse.getAvailable()).isNull();
    }
}