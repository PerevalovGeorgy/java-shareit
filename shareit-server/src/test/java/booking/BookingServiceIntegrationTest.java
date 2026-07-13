package booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.booking.*;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = {
        ShareItServer.class,
        ItemRepository.class,
        UserRepository.class,
        BookingRepository.class})
class BookingServiceIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    private BookingService bookingService;

    private User owner;
    private User booker;
    private Item item;

    @BeforeEach
    void setUp() {
        bookingService = new BookingServiceImp(
                itemRepository,
                userRepository,
                bookingRepository
        );

        owner = User.builder()
                .name("Owner")
                .email("owner@example.com")
                .build();
        entityManager.persist(owner);

        booker = User.builder()
                .name("Booker")
                .email("booker@example.com")
                .build();
        entityManager.persist(booker);

        item = Item.builder()
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .owner(owner)
                .build();
        entityManager.persist(item);

        entityManager.flush();
    }

    @Test
    void create_ShouldSaveBookingToDatabase() {
        BookingRequestDto request = BookingRequestDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        BookingResponseDto saved = bookingService.create(booker.getId(), request);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(Status.WAITING);

        Booking bookingFromDb = entityManager.find(Booking.class, saved.getId());
        assertThat(bookingFromDb).isNotNull();
        assertThat(bookingFromDb.getBooker().getId()).isEqualTo(booker.getId());
        assertThat(bookingFromDb.getItem().getId()).isEqualTo(item.getId());
    }

    @Test
    void create_WhenItemNotAvailable_ShouldThrowException() {
        item.setAvailable(false);
        entityManager.persist(item);
        entityManager.flush();

        BookingRequestDto request = BookingRequestDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        assertThrows(BadRequestException.class, () -> {
            bookingService.create(booker.getId(), request);
        });
    }

    @Test
    void create_WhenOwnerBooks_ShouldThrowException() {
        BookingRequestDto request = BookingRequestDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        assertThrows(AccessDeniedException.class, () -> {
            bookingService.create(owner.getId(), request);
        });
    }

    @Test
    void update_ShouldApproveBooking() {
        Booking booking = Booking.builder()
                .item(item)
                .booker(booker)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(Status.WAITING)
                .createdAt(LocalDateTime.now())
                .build();
        entityManager.persist(booking);
        entityManager.flush();

        BookingResponseDto updated = bookingService.update(owner.getId(), booking.getId(), true);

        assertThat(updated.getStatus()).isEqualTo(Status.APPROVED);

        Booking bookingFromDb = entityManager.find(Booking.class, booking.getId());
        assertThat(bookingFromDb.getStatus()).isEqualTo(Status.APPROVED);
    }

    @Test
    void update_ShouldRejectBooking() {
        Booking booking = Booking.builder()
                .item(item)
                .booker(booker)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(Status.WAITING)
                .createdAt(LocalDateTime.now())
                .build();
        entityManager.persist(booking);
        entityManager.flush();

        BookingResponseDto updated = bookingService.update(owner.getId(), booking.getId(), false);

        assertThat(updated.getStatus()).isEqualTo(Status.REJECTED);
    }

    @Test
    void update_WhenNotOwner_ShouldThrowException() {
        Booking booking = Booking.builder()
                .item(item)
                .booker(booker)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(Status.WAITING)
                .createdAt(LocalDateTime.now())
                .build();
        entityManager.persist(booking);
        entityManager.flush();

        assertThrows(AccessDeniedException.class, () -> {
            bookingService.update(booker.getId(), booking.getId(), true);
        });
    }

    @Test
    void update_WhenAlreadyProcessed_ShouldThrowException() {
        Booking booking = Booking.builder()
                .item(item)
                .booker(booker)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(Status.APPROVED)
                .createdAt(LocalDateTime.now())
                .build();
        entityManager.persist(booking);
        entityManager.flush();

        assertThrows(BadRequestException.class, () -> {
            bookingService.update(owner.getId(), booking.getId(), true);
        });
    }

    @Test
    void get_ShouldReturnBookingForBooker() {
        Booking booking = Booking.builder()
                .item(item)
                .booker(booker)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(Status.WAITING)
                .createdAt(LocalDateTime.now())
                .build();
        entityManager.persist(booking);
        entityManager.flush();

        BookingResponseDto found = bookingService.get(booker.getId(), booking.getId());

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(booking.getId());
    }

    @Test
    void get_ShouldReturnBookingForOwner() {
        Booking booking = Booking.builder()
                .item(item)
                .booker(booker)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(Status.WAITING)
                .createdAt(LocalDateTime.now())
                .build();
        entityManager.persist(booking);
        entityManager.flush();

        BookingResponseDto found = bookingService.get(owner.getId(), booking.getId());

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(booking.getId());
    }

    @Test
    void get_WhenNotBookerOrOwner_ShouldThrowException() {
        User otherUser = User.builder()
                .name("Other")
                .email("other@example.com")
                .build();
        entityManager.persist(otherUser);
        entityManager.flush();

        Booking booking = Booking.builder()
                .item(item)
                .booker(booker)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(Status.WAITING)
                .createdAt(LocalDateTime.now())
                .build();
        entityManager.persist(booking);
        entityManager.flush();

        assertThrows(AccessDeniedException.class, () -> {
            bookingService.get(otherUser.getId(), booking.getId());
        });
    }

    @Test
    void getAllByUser_ShouldReturnAllBookings() {
        Booking booking1 = Booking.builder()
                .item(item)
                .booker(booker)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(Status.WAITING)
                .createdAt(LocalDateTime.now())
                .build();
        entityManager.persist(booking1);

        Booking booking2 = Booking.builder()
                .item(item)
                .booker(booker)
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(4))
                .status(Status.APPROVED)
                .createdAt(LocalDateTime.now())
                .build();
        entityManager.persist(booking2);

        entityManager.flush();

        List<BookingResponseDto> bookings = bookingService.getAllByUser(booker.getId(), "ALL");

        assertThat(bookings).hasSize(2);
    }

    @Test
    void getAllByUser_WithWaitingState_ShouldReturnWaitingBookings() {
        Booking booking1 = Booking.builder()
                .item(item)
                .booker(booker)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(Status.WAITING)
                .createdAt(LocalDateTime.now())
                .build();
        entityManager.persist(booking1);

        Booking booking2 = Booking.builder()
                .item(item)
                .booker(booker)
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(4))
                .status(Status.APPROVED)
                .createdAt(LocalDateTime.now())
                .build();
        entityManager.persist(booking2);

        entityManager.flush();

        List<BookingResponseDto> bookings = bookingService.getAllByUser(booker.getId(), "WAITING");

        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getStatus()).isEqualTo(Status.WAITING);
    }

    @Test
    void getAllByOwner_ShouldReturnAllBookings() {
       Booking booking = Booking.builder()
                .item(item)
                .booker(booker)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(Status.WAITING)
                .createdAt(LocalDateTime.now())
                .build();
        entityManager.persist(booking);
        entityManager.flush();

        List<BookingResponseDto> bookings = bookingService.getAllByOwner(owner.getId(), "ALL");

        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getItem().getId()).isEqualTo(item.getId());
    }
}