package ru.practicum.shareit.request;

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
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = ShareItServer.class)
@AutoConfigureMockMvc
@ContextConfiguration(classes = {ShareItServer.class})
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService itemRequestService;

    private ItemRequestDto itemRequestDto;
    private ItemResponseDto itemResponseDto;

    @BeforeEach
    void setUp() {
        itemResponseDto = ItemResponseDto.builder()
                .id(1L)
                .name("Test Item")
                .ownerId(2L)
                .description("Test Description")
                .available(true)
                .build();

        itemRequestDto = ItemRequestDto.builder()
                .id(1L)
                .description("Need a drill")
                .requestorId(1L)
                .requestorName("Test User")
                .created(LocalDateTime.now())
                .items(Arrays.asList(itemResponseDto))
                .build();
    }

    @Test
    void create_ShouldReturnCreatedRequest() throws Exception {
        ItemRequestDto request = ItemRequestDto.builder()
                .description("Need a drill")
                .build();

        when(itemRequestService.create(anyLong(), any(ItemRequestDto.class)))
                .thenReturn(itemRequestDto);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Need a drill"))
                .andExpect(jsonPath("$.requestorId").value(1))
                .andExpect(jsonPath("$.requestorName").value("Test User"))
                .andExpect(jsonPath("$.items[0].id").value(1))
                .andExpect(jsonPath("$.items[0].name").value("Test Item"));

        verify(itemRequestService, times(1)).create(eq(1L), any(ItemRequestDto.class));
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

        verify(itemRequestService, never()).create(anyLong(), any(ItemRequestDto.class));
    }

    @Test
    void getAllByUser_ShouldReturnListOfRequests() throws Exception {
        List<ItemRequestDto> requests = Arrays.asList(
                ItemRequestDto.builder().id(1L).description("Request 1").requestorId(1L).build(),
                ItemRequestDto.builder().id(2L).description("Request 2").requestorId(1L).build()
        );

        when(itemRequestService.getAllByUser(anyLong())).thenReturn(requests);

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].description").value("Request 1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].description").value("Request 2"));

        verify(itemRequestService, times(1)).getAllByUser(1L);
    }

    @Test
    void getAllByUser_WithNoRequests_ShouldReturnEmptyList() throws Exception {
        when(itemRequestService.getAllByUser(anyLong())).thenReturn(Arrays.asList());

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(itemRequestService, times(1)).getAllByUser(1L);
    }

    @Test
    void getAllByUser_WithoutHeader_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/requests"))
                .andExpect(status().isBadRequest());

        verify(itemRequestService, never()).getAllByUser(anyLong());
    }

    @Test
    void getAllOtherUsers_ShouldReturnListOfRequests() throws Exception {
        List<ItemRequestDto> requests = Arrays.asList(
                ItemRequestDto.builder().id(1L).description("Request 1").requestorId(2L).build(),
                ItemRequestDto.builder().id(2L).description("Request 2").requestorId(3L).build()
        );

        when(itemRequestService.getAllOtherUsers(anyLong())).thenReturn(requests);

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].requestorId").value(2))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].requestorId").value(3));

        verify(itemRequestService, times(1)).getAllOtherUsers(1L);
    }

    @Test
    void getAllOtherUsers_WithoutHeader_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/requests/all"))
                .andExpect(status().isBadRequest());

        verify(itemRequestService, never()).getAllOtherUsers(anyLong());
    }

    @Test
    void getById_ShouldReturnRequest() throws Exception {
        when(itemRequestService.getById(anyLong(), anyLong())).thenReturn(itemRequestDto);

        mockMvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Need a drill"))
                .andExpect(jsonPath("$.requestorId").value(1))
                .andExpect(jsonPath("$.items[0].id").value(1))
                .andExpect(jsonPath("$.items[0].name").value("Test Item"));

        verify(itemRequestService, times(1)).getById(1L, 1L);
    }

    @Test
    void getById_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        when(itemRequestService.getById(anyLong(), anyLong()))
                .thenThrow(new NotFoundException("Request not found"));

        mockMvc.perform(get("/requests/999")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getById_WithoutHeader_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/requests/1"))
                .andExpect(status().isBadRequest());

        verify(itemRequestService, never()).getById(anyLong(), anyLong());
    }
}