package ru.practicum.shareit.booking;

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
import ru.practicum.shareit.exception.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = ShareitServer.class)
@AutoConfigureMockMvc
@ContextConfiguration(classes = {ShareitServer.class})
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private BookingRequestDto bookingRequestDto;
    private BookingResponseDto bookingResponseDto;

    @BeforeEach
    void setUp() {
        bookingRequestDto = BookingRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        bookingResponseDto = BookingResponseDto.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(Status.WAITING)
                .createdAt(LocalDateTime.now())
                .build();
    }


    @Test
    void create_ShouldReturnCreatedBooking() throws Exception {
        when(bookingService.create(anyLong(), any(BookingRequestDto.class)))
                .thenReturn(bookingResponseDto);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("WAITING"));

        verify(bookingService, times(1)).create(eq(1L), any(BookingRequestDto.class));
    }

    @Test
    void create_WithInvalidDates_ShouldReturnBadRequest() throws Exception {
        BookingRequestDto invalidRequest = BookingRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(1))
                .build();

        when(bookingService.create(anyLong(), any(BookingRequestDto.class)))
                .thenThrow(new ValidationException("Дата начала должна быть раньше даты окончания"));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_WithoutHeader_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_ShouldReturnUpdatedBooking() throws Exception {
        BookingResponseDto approvedBooking = BookingResponseDto.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(Status.APPROVED)
                .createdAt(LocalDateTime.now())
                .build();

        when(bookingService.update(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(approvedBooking);

        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", "1")
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(bookingService, times(1)).update(eq(1L), eq(1L), eq(true));
    }

    @Test
    void update_WithReject_ShouldReturnRejectedBooking() throws Exception {
        BookingResponseDto rejectedBooking = BookingResponseDto.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(Status.REJECTED)
                .createdAt(LocalDateTime.now())
                .build();

        when(bookingService.update(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(rejectedBooking);

        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", "1")
                        .param("approved", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));

        verify(bookingService, times(1)).update(eq(1L), eq(1L), eq(false));
    }

    @Test
    void update_WhenNotOwner_ShouldReturnForbidden() throws Exception {
        when(bookingService.update(anyLong(), anyLong(), anyBoolean()))
                .thenThrow(new AccessDeniedException("Подтвердить бронирование может только владелец"));

        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", "2")
                        .param("approved", "true"))
                .andExpect(status().isForbidden());
    }

    @Test
    void update_WhenAlreadyProcessed_ShouldReturnBadRequest() throws Exception {
        when(bookingService.update(anyLong(), anyLong(), anyBoolean()))
                .thenThrow(new BadRequestException("Бронирование уже обработано"));

        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", "1")
                        .param("approved", "true"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void get_ShouldReturnBooking() throws Exception {
        when(bookingService.get(anyLong(), anyLong())).thenReturn(bookingResponseDto);

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("WAITING"));

        verify(bookingService, times(1)).get(1L, 1L);
    }

    @Test
    void get_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        when(bookingService.get(anyLong(), anyLong()))
                .thenThrow(new NotFoundException("Бронирование с id 999 не найдено"));

        mockMvc.perform(get("/bookings/999")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void get_WhenNotBookerOrOwner_ShouldReturnForbidden() throws Exception {
        when(bookingService.get(anyLong(), anyLong()))
                .thenThrow(new AccessDeniedException("Пользователь не имеет доступа к этому бронированию"));

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", "3"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllByUser_ShouldReturnListOfBookings() throws Exception {
        List<BookingResponseDto> bookings = Arrays.asList(
                BookingResponseDto.builder().id(1L).status(Status.WAITING).build(),
                BookingResponseDto.builder().id(2L).status(Status.APPROVED).build()
        );

        when(bookingService.getAllByUser(anyLong(), anyString())).thenReturn(bookings);

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", "1")
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(bookingService, times(1)).getAllByUser(1L, "ALL");
    }

    @Test
    void getAllByUser_WithWaitingState_ShouldReturnWaitingBookings() throws Exception {
        List<BookingResponseDto> bookings = Arrays.asList(
                BookingResponseDto.builder().id(1L).status(Status.WAITING).build()
        );

        when(bookingService.getAllByUser(anyLong(), anyString())).thenReturn(bookings);

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", "1")
                        .param("state", "WAITING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("WAITING"));

        verify(bookingService, times(1)).getAllByUser(1L, "WAITING");
    }

    @Test
    void getAllByUser_WithInvalidState_ShouldReturnBadRequest() throws Exception {
        when(bookingService.getAllByUser(anyLong(), anyString()))
                .thenThrow(new IllegalArgumentException("Неизвестный статус: INVALID"));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", "1")
                        .param("state", "INVALID"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllByOwner_ShouldReturnListOfBookings() throws Exception {
        List<BookingResponseDto> bookings = Arrays.asList(
                BookingResponseDto.builder().id(1L).status(Status.WAITING).build(),
                BookingResponseDto.builder().id(2L).status(Status.APPROVED).build()
        );

        when(bookingService.getAllByOwner(anyLong(), anyString())).thenReturn(bookings);

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", "1")
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(bookingService, times(1)).getAllByOwner(1L, "ALL");
    }

    @Test
    void getAllByOwner_WhenNoItems_ShouldReturnBadRequest() throws Exception {
        when(bookingService.getAllByOwner(anyLong(), anyString()))
                .thenThrow(new BadRequestException("у пользователя нет вещей"));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", "1")
                        .param("state", "ALL"))
                .andExpect(status().isBadRequest());
    }
}