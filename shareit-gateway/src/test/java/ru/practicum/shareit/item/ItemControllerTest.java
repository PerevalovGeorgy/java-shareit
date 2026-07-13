package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.ShareitGateway;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = ShareitGateway.class)
@AutoConfigureMockMvc
@ContextConfiguration(classes = {ShareitGateway.class})
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemClient itemClient;

    private ItemDto itemDto;
    private CommentTextDto commentTextDto;

    @BeforeEach
    void setUp() {
        itemDto = ItemDto.builder()
                .id(1L)
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .ownerId(1L)
                .build();

        commentTextDto = CommentTextDto.builder()
                .text("Great item!")
                .build();
    }

    @Test
    void create_ShouldReturnCreatedItem() throws Exception {
        ItemDto request = ItemDto.builder()
                .name("New Item")
                .description("New Description")
                .available(true)
                .build();

        when(itemClient.create(anyLong(), any(ItemDto.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(itemDto));

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Item"));

        verify(itemClient, times(1)).create(eq(1L), any(ItemDto.class));
    }

    @Test
    void create_WithoutName_ShouldReturnBadRequest() throws Exception {
        ItemDto invalidItem = ItemDto.builder()
                .description("Description")
                .available(true)
                .build();

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidItem)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).create(anyLong(), any(ItemDto.class));
    }

    @Test
    void create_WithoutHeader_ShouldReturnBadRequest() throws Exception {
        ItemDto request = ItemDto.builder()
                .name("New Item")
                .description("New Description")
                .available(true)
                .build();

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).create(anyLong(), any(ItemDto.class));
    }

    @Test
    void update_ShouldReturnUpdatedItem() throws Exception {
        ItemDto updateRequest = ItemDto.builder()
                .name("Updated Name")
                .build();

        when(itemClient.update(anyLong(), anyLong(), any(ItemDto.class)))
                .thenReturn(ResponseEntity.ok(itemDto));

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(itemClient, times(1)).update(eq(1L), eq(1L), any(ItemDto.class));
    }

    @Test
    void findById_ShouldReturnItem() throws Exception {
        when(itemClient.findById(anyLong(), anyLong()))
                .thenReturn(ResponseEntity.ok(itemDto));

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Item"));

        verify(itemClient, times(1)).findById(1L, 1L);
    }

    @Test
    void findAllByOwner_ShouldReturnListOfItems() throws Exception {
        List<ItemDto> items = Arrays.asList(
                ItemDto.builder().id(1L).name("Item1").build(),
                ItemDto.builder().id(2L).name("Item2").build()
        );

        when(itemClient.findAllByOwner(anyLong()))
                .thenReturn(ResponseEntity.ok(items));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(itemClient, times(1)).findAllByOwner(1L);
    }

    @Test
    void search_ShouldReturnMatchingItems() throws Exception {
        List<ItemDto> items = Arrays.asList(
                ItemDto.builder().id(1L).name("Item1").build()
        );

        when(itemClient.search(anyString())).thenReturn(ResponseEntity.ok(items));

        mockMvc.perform(get("/items/search")
                        .param("text", "Item"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(itemClient, times(1)).search("Item");
    }

    @Test
    void delete_ShouldReturnNoContent() throws Exception {
        when(itemClient.delete(anyLong(), anyLong()))
                .thenReturn(ResponseEntity.noContent().build());

        mockMvc.perform(delete("/items/1")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isNoContent());

        verify(itemClient, times(1)).delete(1L, 1L);
    }

    @Test
    void addComment_ShouldReturnCreatedComment() throws Exception {
        CommentDto commentDto = CommentDto.builder()
                .id(1L)
                .text("Great item!")
                .authorName("User2")
                .build();

        when(itemClient.addComment(anyLong(), anyLong(), any(CommentTextDto.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(commentDto));

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentTextDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("Great item!"));

        verify(itemClient, times(1)).addComment(eq(2L), eq(1L), any(CommentTextDto.class));
    }
}