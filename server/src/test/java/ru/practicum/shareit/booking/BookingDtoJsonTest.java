package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@ContextConfiguration(classes = {
        BookingRequestDto.class,
        BookingResponseDto.class,
        BookingShortDto.class,
})
class BookingDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
    }

    @Test
    void serializeBookingRequestDto_ShouldReturnValidJson() throws Exception {
        BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.of(2026, 7, 15, 10, 0, 0))
                .end(LocalDateTime.of(2026, 7, 16, 10, 0, 0))
                .build();

        String json = objectMapper.writeValueAsString(requestDto);

        assertThat(json).contains("\"itemId\":1");
        assertThat(json).contains("\"start\":\"2026-07-15T10:00:00\"");
        assertThat(json).contains("\"end\":\"2026-07-16T10:00:00\"");
    }

    @Test
    void deserializeBookingRequestDto_ShouldReturnValidObject() throws Exception {
        String json = "{\"itemId\":1,\"start\":\"2026-07-15T10:00:00\",\"end\":\"2026-07-16T10:00:00\"}";

        BookingRequestDto requestDto = objectMapper.readValue(json, BookingRequestDto.class);

        assertThat(requestDto.getItemId()).isEqualTo(1L);
        assertThat(requestDto.getStart()).isEqualTo(LocalDateTime.of(2026, 7, 15, 10, 0, 0));
        assertThat(requestDto.getEnd()).isEqualTo(LocalDateTime.of(2026, 7, 16, 10, 0, 0));
    }

    @Test
    void deserializeBookingRequestDto_WithNullFields_ShouldReturnValidObject() throws Exception {
        String json = "{\"itemId\":1}";

        BookingRequestDto requestDto = objectMapper.readValue(json, BookingRequestDto.class);

        assertThat(requestDto.getItemId()).isEqualTo(1L);
        assertThat(requestDto.getStart()).isNull();
        assertThat(requestDto.getEnd()).isNull();
    }

    @Test
    void serializeBookingResponseDto_ShouldReturnValidJson() throws Exception {
        BookingResponseDto responseDto = BookingResponseDto.builder()
                .id(1L)
                .start(LocalDateTime.of(2026, 7, 15, 10, 0, 0))
                .end(LocalDateTime.of(2026, 7, 16, 10, 0, 0))
                .status(Status.WAITING)
                .createdAt(LocalDateTime.of(2026, 7, 13, 10, 0, 0))
                .build();

        String json = objectMapper.writeValueAsString(responseDto);

        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"status\":\"WAITING\"");
        assertThat(json).contains("\"createdAt\":\"2026-07-13T10:00:00\"");
    }

    @Test
    void deserializeBookingResponseDto_ShouldReturnValidObject() throws Exception {
        String json = "{\"id\":1,\"status\":\"WAITING\"}";

        BookingResponseDto responseDto = objectMapper.readValue(json, BookingResponseDto.class);

        assertThat(responseDto.getId()).isEqualTo(1L);
        assertThat(responseDto.getStatus()).isEqualTo(Status.WAITING);
    }

    @Test
    void deserializeBookingResponseDto_WithAllFields_ShouldReturnValidObject() throws Exception {
        String json = "{\"id\":1,\"start\":\"2026-07-15T10:00:00\",\"end\":\"2026-07-16T10:00:00\",\"status\":\"APPROVED\",\"createdAt\":\"2026-07-13T10:00:00\"}";

        BookingResponseDto responseDto = objectMapper.readValue(json, BookingResponseDto.class);

        assertThat(responseDto.getId()).isEqualTo(1L);
        assertThat(responseDto.getStart()).isEqualTo(LocalDateTime.of(2026, 7, 15, 10, 0, 0));
        assertThat(responseDto.getEnd()).isEqualTo(LocalDateTime.of(2026, 7, 16, 10, 0, 0));
        assertThat(responseDto.getStatus()).isEqualTo(Status.APPROVED);
        assertThat(responseDto.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 7, 13, 10, 0, 0));
    }

    @Test
    void serializeBookingShortDto_ShouldReturnValidJson() throws Exception {
        BookingShortDto shortDto = BookingShortDto.builder()
                .id(1L)
                .bookerId(2L)
                .bookerName("Booker")
                .start(LocalDateTime.of(2026, 7, 15, 10, 0, 0))
                .end(LocalDateTime.of(2026, 7, 16, 10, 0, 0))
                .status(Status.APPROVED)
                .build();

        String json = objectMapper.writeValueAsString(shortDto);

        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"bookerId\":2");
        assertThat(json).contains("\"bookerName\":\"Booker\"");
        assertThat(json).contains("\"status\":\"APPROVED\"");
    }

    @Test
    void deserializeBookingShortDto_ShouldReturnValidObject() throws Exception {
        String json = "{\"id\":1,\"bookerId\":2,\"bookerName\":\"Booker\",\"status\":\"APPROVED\"}";

        BookingShortDto shortDto = objectMapper.readValue(json, BookingShortDto.class);

        assertThat(shortDto.getId()).isEqualTo(1L);
        assertThat(shortDto.getBookerId()).isEqualTo(2L);
        assertThat(shortDto.getBookerName()).isEqualTo("Booker");
        assertThat(shortDto.getStatus()).isEqualTo(Status.APPROVED);
    }

    @Test
    void serializeBookingResponseDto_WithNullStatus_ShouldReturnValidJson() throws Exception {
        BookingResponseDto responseDto = BookingResponseDto.builder()
                .id(1L)
                .build();

        String json = objectMapper.writeValueAsString(responseDto);

        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"status\":null");
    }

    @Test
    void deserializeBookingResponseDto_WithUnknownStatus_ShouldReturnNull() throws Exception {
        String json = "{\"id\":1,\"status\":\"UNKNOWN\"}";

        BookingResponseDto responseDto = objectMapper.readValue(json, BookingResponseDto.class);

        assertThat(responseDto.getId()).isEqualTo(1L);
        assertThat(responseDto.getStatus()).isNull();
    }
}