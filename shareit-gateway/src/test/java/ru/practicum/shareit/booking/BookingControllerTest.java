package ru.practicum.shareit.booking;

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
import ru.practicum.shareit.ShareItGateway;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = ShareItGateway.class)
@AutoConfigureMockMvc
@ContextConfiguration(classes = {ShareItGateway.class})
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingClient bookingClient;

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
                .status(Status.WAITING)
                .build();
    }

    @Test
    void create_ShouldReturnCreatedBooking() throws Exception {
        when(bookingClient.create(anyLong(), any(BookingRequestDto.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(bookingResponseDto));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("WAITING"));

        verify(bookingClient, times(1)).create(eq(1L), any(BookingRequestDto.class));
    }

    @Test
    void create_WithoutHeader_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequestDto)))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).create(anyLong(), any(BookingRequestDto.class));
    }

    @Test
    void update_ShouldReturnUpdatedBooking() throws Exception {
        BookingResponseDto approvedBooking = BookingResponseDto.builder()
                .id(1L)
                .status(Status.APPROVED)
                .build();

        when(bookingClient.update(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(ResponseEntity.ok(approvedBooking));

        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", "1")
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(bookingClient, times(1)).update(eq(1L), eq(1L), eq(true));
    }

    @Test
    void update_WithoutHeader_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(patch("/bookings/1")
                        .param("approved", "true"))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).update(anyLong(), anyLong(), anyBoolean());
    }

    @Test
    void get_ShouldReturnBooking() throws Exception {
        when(bookingClient.findById(anyLong(), anyLong()))
                .thenReturn(ResponseEntity.ok(bookingResponseDto));

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(bookingClient, times(1)).findById(1L, 1L);
    }

    @Test
    void findAllByUser_ShouldReturnListOfBookings() throws Exception {
        List<BookingResponseDto> bookings = Arrays.asList(
                BookingResponseDto.builder().id(1L).status(Status.WAITING).build(),
                BookingResponseDto.builder().id(2L).status(Status.APPROVED).build()
        );

        when(bookingClient.findAllByUser(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(ResponseEntity.ok(bookings));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", "1")
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(bookingClient, times(1)).findAllByUser(eq(1L), eq("ALL"), eq(0), eq(10));
    }

    @Test
    void findAllByOwner_ShouldReturnListOfBookings() throws Exception {
        List<BookingResponseDto> bookings = Arrays.asList(
                BookingResponseDto.builder().id(1L).status(Status.WAITING).build()
        );

        when(bookingClient.findAllByOwner(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(ResponseEntity.ok(bookings));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", "1")
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(bookingClient, times(1)).findAllByOwner(eq(1L), eq("ALL"), eq(0), eq(10));
    }
}