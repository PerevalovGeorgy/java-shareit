package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.TestConfig;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRequestController.class)
@Import(TestConfig.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestClient itemRequestClient;

    private ItemRequestDto itemRequestDto;

    @BeforeEach
    void setUp() {
        itemRequestDto = ItemRequestDto.builder()
                .id(1L)
                .description("Need a drill")
                .requestorId(1L)
                .requestorName("Test User")
                .created(LocalDateTime.now())
                .build();
    }

    @Test
    void create_ShouldReturnCreatedRequest() throws Exception {
        ItemRequestDto request = ItemRequestDto.builder()
                .description("Need a drill")
                .build();

        when(itemRequestClient.create(anyLong(), any(ItemRequestDto.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(itemRequestDto));

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Need a drill"));

        verify(itemRequestClient, times(1)).create(eq(1L), any(ItemRequestDto.class));
    }

    @Test
    void create_WithEmptyDescription_ShouldReturnBadRequest() throws Exception {
        ItemRequestDto invalidRequest = ItemRequestDto.builder()
                .description("")
                .build();

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(itemRequestClient, never()).create(anyLong(), any(ItemRequestDto.class));
    }

    @Test
    void create_WithoutHeader_ShouldReturnBadRequest() throws Exception {
        ItemRequestDto request = ItemRequestDto.builder()
                .description("Need a drill")
                .build();

        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(itemRequestClient, never()).create(anyLong(), any(ItemRequestDto.class));
    }

    @Test
    void getAllByUser_ShouldReturnListOfRequests() throws Exception {
        List<ItemRequestDto> requests = Arrays.asList(
                ItemRequestDto.builder().id(1L).description("Request 1").build(),
                ItemRequestDto.builder().id(2L).description("Request 2").build()
        );

        when(itemRequestClient.getAllByUser(anyLong()))
                .thenReturn(ResponseEntity.ok(requests));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(itemRequestClient, times(1)).getAllByUser(1L);
    }

    @Test
    void getAllByUser_WithoutHeader_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/requests"))
                .andExpect(status().isBadRequest());

        verify(itemRequestClient, never()).getAllByUser(anyLong());
    }

    @Test
    void getAllOtherUsers_ShouldReturnListOfRequests() throws Exception {
        List<ItemRequestDto> requests = Arrays.asList(
                ItemRequestDto.builder().id(1L).description("Request 1").build()
        );

        when(itemRequestClient.getAllOtherUsers(anyLong()))
                .thenReturn(ResponseEntity.ok(requests));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(itemRequestClient, times(1)).getAllOtherUsers(1L);
    }

    @Test
    void getById_ShouldReturnRequest() throws Exception {
        when(itemRequestClient.getById(anyLong(), anyLong()))
                .thenReturn(ResponseEntity.ok(itemRequestDto));

        mockMvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Need a drill"));

        verify(itemRequestClient, times(1)).getById(1L, 1L);
    }

    @Test
    void getById_WithoutHeader_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/requests/1"))
                .andExpect(status().isBadRequest());

        verify(itemRequestClient, never()).getById(anyLong(), anyLong());
    }

    @Test
    void getById_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        when(itemRequestClient.getById(anyLong(), anyLong()))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).build());

        mockMvc.perform(get("/requests/999")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isNotFound());

        verify(itemRequestClient, times(1)).getById(1L, 999L);
    }
}