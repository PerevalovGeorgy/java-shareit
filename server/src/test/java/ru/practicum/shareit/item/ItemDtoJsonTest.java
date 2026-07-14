package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@ContextConfiguration(classes = {ItemDto.class, CommentDto.class})
class ItemDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void serializeItemDto_ShouldReturnValidJson() throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .ownerId(1L)
                .requestId(2L)
                .comments(Arrays.asList(
                        CommentDto.builder()
                                .id(1L)
                                .text("Great!")
                                .authorName("User")
                                .created(LocalDateTime.now())
                                .build()
                ))
                .build();

        String json = objectMapper.writeValueAsString(itemDto);

        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"name\":\"Test Item\"");
        assertThat(json).contains("\"description\":\"Test Description\"");
        assertThat(json).contains("\"available\":true");
        assertThat(json).contains("\"ownerId\":1");
        assertThat(json).contains("\"comments\"");
    }

    @Test
    void deserializeItemDto_ShouldReturnValidObject() throws Exception {
        String json = "{\"id\":1,\"name\":\"Test Item\",\"description\":\"Test Description\",\"available\":true,\"ownerId\":1}";

        ItemDto itemDto = objectMapper.readValue(json, ItemDto.class);

        assertThat(itemDto.getId()).isEqualTo(1L);
        assertThat(itemDto.getName()).isEqualTo("Test Item");
        assertThat(itemDto.getDescription()).isEqualTo("Test Description");
        assertThat(itemDto.getAvailable()).isTrue();
        assertThat(itemDto.getOwnerId()).isEqualTo(1L);
    }

    @Test
    void deserializeItemDto_WithNullFields_ShouldReturnValidObject() throws Exception {
        String json = "{\"id\":1,\"name\":\"Test Item\"}";

        ItemDto itemDto = objectMapper.readValue(json, ItemDto.class);

        assertThat(itemDto.getId()).isEqualTo(1L);
        assertThat(itemDto.getName()).isEqualTo("Test Item");
        assertThat(itemDto.getDescription()).isNull();
        assertThat(itemDto.getAvailable()).isNull();
    }

    @Test
    void serializeCommentDto_ShouldReturnValidJson() throws Exception {
        CommentDto commentDto = CommentDto.builder()
                .id(1L)
                .text("Great item!")
                .authorName("User2")
                .created(LocalDateTime.of(2026, 7, 13, 10, 0, 0))
                .build();

        String json = objectMapper.writeValueAsString(commentDto);

        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"text\":\"Great item!\"");
        assertThat(json).contains("\"authorName\":\"User2\"");
        assertThat(json).contains("\"created\":\"2026-07-13T10:00:00\"");
    }

    @Test
    void deserializeCommentDto_ShouldReturnValidObject() throws Exception {
        String json = "{\"id\":1,\"text\":\"Great item!\",\"authorName\":\"User2\"}";

        CommentDto commentDto = objectMapper.readValue(json, CommentDto.class);

        assertThat(commentDto.getId()).isEqualTo(1L);
        assertThat(commentDto.getText()).isEqualTo("Great item!");
        assertThat(commentDto.getAuthorName()).isEqualTo("User2");
    }

    @Test
    void deserializeCommentDto_WithNullFields_ShouldReturnValidObject() throws Exception {
        String json = "{\"id\":1,\"text\":\"Great item!\"}";

        CommentDto commentDto = objectMapper.readValue(json, CommentDto.class);

        assertThat(commentDto.getId()).isEqualTo(1L);
        assertThat(commentDto.getText()).isEqualTo("Great item!");
        assertThat(commentDto.getAuthorName()).isNull();
        assertThat(commentDto.getCreated()).isNull();
    }

    @Test
    void serializeItemDto_WithEmptyComments_ShouldReturnValidJson() throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .ownerId(1L)
                .comments(Arrays.asList())
                .build();

        String json = objectMapper.writeValueAsString(itemDto);

        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"name\":\"Test Item\"");
        assertThat(json).contains("\"comments\":[]");
    }

    @Test
    void serializeItemDto_WithAvailableFalse_ShouldReturnValidJson() throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("Test Item")
                .description("Test Description")
                .available(false)
                .ownerId(1L)
                .build();

        String json = objectMapper.writeValueAsString(itemDto);

        assertThat(json).contains("\"available\":false");
    }

    @Test
    void deserializeCommentDto_WithDateTime_ShouldReturnValidObject() throws Exception {
        String json = "{\"id\":1,\"text\":\"Great item!\",\"authorName\":\"User2\",\"created\":\"2026-07-13T10:00:00\"}";

        CommentDto commentDto = objectMapper.readValue(json, CommentDto.class);

        assertThat(commentDto.getId()).isEqualTo(1L);
        assertThat(commentDto.getText()).isEqualTo("Great item!");
        assertThat(commentDto.getAuthorName()).isEqualTo("User2");
        assertThat(commentDto.getCreated()).isEqualTo(LocalDateTime.of(2026, 7, 13, 10, 0, 0));
    }
}