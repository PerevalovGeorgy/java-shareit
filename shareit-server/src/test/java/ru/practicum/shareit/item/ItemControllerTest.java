package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.ShareitServer;
import ru.practicum.shareit.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = ShareitServer.class)
@AutoConfigureMockMvc
@ContextConfiguration(classes = {ShareitServer.class})
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    private ItemDto itemDto;
    private CommentDto commentDto;
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

        commentDto = CommentDto.builder()
                .id(1L)
                .text("Great item!")
                .authorName("User2")
                .created(LocalDateTime.now())
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

        when(itemService.create(anyLong(), any(ItemDto.class))).thenReturn(itemDto);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Item"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.ownerId").value(1));

        verify(itemService, times(1)).create(eq(1L), any(ItemDto.class));
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

        verify(itemService, never()).create(anyLong(), any(ItemDto.class));
    }

    @Test
    void update_ShouldReturnUpdatedItem() throws Exception {
        ItemDto updateRequest = ItemDto.builder()
                .name("Updated Name")
                .description("Updated Description")
                .available(false)
                .build();

        ItemDto updatedItem = ItemDto.builder()
                .id(1L)
                .name("Updated Name")
                .description("Updated Description")
                .available(false)
                .ownerId(1L)
                .build();

        when(itemService.update(anyLong(), anyLong(), any(ItemDto.class))).thenReturn(updatedItem);

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.available").value(false));

        verify(itemService, times(1)).update(eq(1L), eq(1L), any(ItemDto.class));
    }

    @Test
    void update_WithoutHeader_ShouldReturnBadRequest() throws Exception {
        ItemDto updateRequest = ItemDto.builder()
                .name("Updated Name")
                .build();

        mockMvc.perform(patch("/items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());

        verify(itemService, never()).update(anyLong(), anyLong(), any(ItemDto.class));
    }

    @Test
    void findById_ShouldReturnItem() throws Exception {
        when(itemService.findById(anyLong(), anyLong())).thenReturn(itemDto);

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Item"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.available").value(true));

        verify(itemService, times(1)).findById(1L, 1L);
    }

    @Test
    void findById_WithoutHeader_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/items/1"))
                .andExpect(status().isBadRequest());

        verify(itemService, never()).findById(anyLong(), anyLong());
    }

    @Test
    void findById_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        when(itemService.findById(anyLong(), anyLong()))
                .thenThrow(new NotFoundException("Item not found"));

        mockMvc.perform(get("/items/999")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void findAllByOwner_ShouldReturnListOfItems() throws Exception {
        Collection<ItemDto> items = Arrays.asList(
                ItemDto.builder().id(1L).name("Item1").description("Desc1").available(true).ownerId(1L).build(),
                ItemDto.builder().id(2L).name("Item2").description("Desc2").available(false).ownerId(1L).build()
        );

        when(itemService.findAllByOwner(anyLong())).thenReturn(items);

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Item1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Item2"));

        verify(itemService, times(1)).findAllByOwner(1L);
    }

    @Test
    void findAllByOwner_WithNoItems_ShouldReturnEmptyList() throws Exception {
        when(itemService.findAllByOwner(anyLong())).thenReturn(Arrays.asList());

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(itemService, times(1)).findAllByOwner(1L);
    }


    @Test
    void search_ShouldReturnMatchingItems() throws Exception {
        Collection<ItemDto> items = Arrays.asList(
                ItemDto.builder().id(1L).name("Item1").description("Description").available(true).ownerId(1L).build()
        );

        when(itemService.searchAvailable(anyString())).thenReturn(items);

        mockMvc.perform(get("/items/search")
                        .param("text", "Item"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Item1"));

        verify(itemService, times(1)).searchAvailable("Item");
    }

    @Test
    void search_WithEmptyText_ShouldReturnEmptyList() throws Exception {
        when(itemService.searchAvailable(anyString())).thenReturn(Arrays.asList());

        mockMvc.perform(get("/items/search")
                        .param("text", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }


    @Test
    void delete_ShouldReturnNoContent() throws Exception {
        doNothing().when(itemService).delete(anyLong(), anyLong());

        mockMvc.perform(delete("/items/1")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isNoContent());

        verify(itemService, times(1)).delete(1L, 1L);
    }

    @Test
    void delete_WithoutHeader_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(delete("/items/1"))
                .andExpect(status().isBadRequest());

        verify(itemService, never()).delete(anyLong(), anyLong());
    }

    @Test
    void addComment_ShouldReturnCreatedComment() throws Exception {
        when(itemService.addComment(anyLong(), anyLong(), any(CommentTextDto.class)))
                .thenReturn(commentDto);

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentTextDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("Great item!"))
                .andExpect(jsonPath("$.authorName").value("User2"));

        verify(itemService, times(1)).addComment(eq(2L), eq(1L), any(CommentTextDto.class));
    }

    @Test
    void addComment_WithoutHeader_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/items/1/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentTextDto)))
                .andExpect(status().isBadRequest());
    }
}