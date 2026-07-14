package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.client.BaseClient;

@Service
public class BookingClient extends BaseClient {

    public BookingClient(@Value("${shareit-server.url}") String serverUrl, RestTemplate rest) {
        super(serverUrl, rest);
    }

    public ResponseEntity<Object> create(long userId, BookingRequestDto bookingRequestDto) {
        return post("/bookings", userId, bookingRequestDto, Object.class);
    }

    public ResponseEntity<Object> update(long userId, long bookingId, boolean approved) {
        return patch("/bookings/" + bookingId + "?approved=" + approved, userId, null, Object.class, bookingId, approved);
    }

    public ResponseEntity<Object> findById(long userId, long bookingId) {
        return get("/bookings/" + bookingId, userId, Object.class, bookingId);
    }

    public ResponseEntity<Object> findAllByUser(long userId, String state, Integer from, Integer size) {
        return get("/bookings?state=" + state + "&from=" + from + "&size=" + size, userId, Object.class, state, from, size);
    }

    public ResponseEntity<Object> findAllByOwner(long userId, String state, Integer from, Integer size) {
        return get("/bookings/owner?state=" + state + "&from=" + from + "&size=" + size, userId, Object.class, state, from, size);
    }
}