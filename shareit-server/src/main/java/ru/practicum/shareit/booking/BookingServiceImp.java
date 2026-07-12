package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImp implements BookingService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional
    public BookingResponseDto create(Long userId, BookingRequestDto bookingRequestDto) {
        log.info("Добавление бронирования вещи: {}, пользователем: {}", bookingRequestDto, userId);

        User booker = checkUserExists(userId);
        Item item = checkItemExists(bookingRequestDto.getItemId());

        if (item.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Владелец не может забронировать свою вещь");
        }

        if (!item.isAvailable()) {
            throw new BadRequestException("Вещь недоступна для бронирования");
        }

        if (bookingRequestDto.getStart().isAfter(bookingRequestDto.getEnd()) ||
                bookingRequestDto.getStart().equals(bookingRequestDto.getEnd())) {
            throw new ValidationException("Дата начала должна быть раньше даты окончания");
        }

        if (bookingRepository.existsOverlappingBooking(
                item.getId(), bookingRequestDto.getStart(), bookingRequestDto.getEnd())) {
            throw new BadRequestException("Вещь уже забронирована на указанные даты");
        }

        Booking booking = BookingMapper.toEntity(bookingRequestDto, item, booker);
        Booking created = bookingRepository.save(booking);
        log.info("Успешно добавление бронирования вещи: {}, пользователем: {}", bookingRequestDto, userId);

        return BookingMapper.toResponseDto(created);
    }

    @Override
    @Transactional
    public BookingResponseDto approve(Long userId, Long bookingId, Boolean approved) {
        log.info("Подтверждение бронирования {} пользователем {}, approved={}", bookingId, userId, approved);

        Booking booking = checkBookingExists(bookingId);
        Long ownerId = booking.getItem().getOwner().getId();

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Подтвердить бронирование может только владелец");
        }

        if (booking.getStatus() != Status.WAITING) {
            throw new BadRequestException("Бронирование уже обработано. Текущий статус: " + booking.getStatus());
        }

        if (approved) {
            booking.setStatus(Status.APPROVED);
        } else {
            booking.setStatus(Status.REJECTED);
        }

        Booking updateBooking = bookingRepository.save(booking);
        log.info("Бронирование {} обновлено. Статус: {}", bookingId, updateBooking.getStatus());

        return BookingMapper.toResponseDto(updateBooking);
    }

    @Override
    public BookingResponseDto get(Long userId, Long bookingId) {
        log.info("Получение данных о конкретном бронировании {} пользователя: {}",bookingId, userId);

        checkUserExists(userId);
        Booking booking = checkBookingExists(bookingId);

        boolean isBooker = booking.getBooker().getId().equals(userId);
        boolean isOwner = booking.getItem().getOwner().getId().equals(userId);
        if (!isBooker && !isOwner) {
            throw new AccessDeniedException("Пользователь не имеет доступа к этому бронированию");
        }

        return BookingMapper.toResponseDto(booking);
    }

    @Override
    public List<BookingResponseDto> getAllByUser(Long userId, String status) {
        log.info("Получение данных о забронированных вещая в статусе {}  пользователем {} ", status, userId);

        checkUserExists(userId);
        Status stateEnum = Status.parse(status);
        List<Booking> bookings;

        switch (stateEnum) {
            case ALL:
                bookings = bookingRepository.findByBookerIdOrderByStartDesc(userId);
                break;
            case WAITING:
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, Status.WAITING);
                break;
            case APPROVED:
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, Status.APPROVED);
                break;
            case REJECTED:
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, Status.REJECTED);
                break;
            case CANCELED:
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, Status.CANCELED);
                break;
            default:
                throw new IllegalArgumentException("Неизвестный статус: " + status);
        }

        return bookings.stream()
                .map(BookingMapper::toResponseDto)
                .toList();
    }


    @Override
    public List<BookingResponseDto> getAllByOwner(Long userId, String status) {
        log.info("Получение данных о забронированных вещая в статусе {}  у владельца {} ", status, userId);

        checkUserExists(userId);

        if (itemRepository.findByOwnerId(userId).isEmpty()) {
            throw new BadRequestException("у пользователя нет вещей");
        }

        Status stateEnum = Status.parse(status);
        List<Booking> bookings;

        switch (stateEnum) {
            case ALL:
                bookings = bookingRepository.findAllByItemOwnerId(userId);
                break;
            case WAITING:
                bookings = bookingRepository.findAllByItemOwnerIdAndStatus(userId, Status.WAITING);
                break;
            case APPROVED:
                bookings = bookingRepository.findAllByItemOwnerIdAndStatus(userId, Status.APPROVED);
                break;
            case REJECTED:
                bookings = bookingRepository.findAllByItemOwnerIdAndStatus(userId, Status.REJECTED);
                break;
            case CANCELED:
                bookings = bookingRepository.findAllByItemOwnerIdAndStatus(userId, Status.CANCELED);
                break;
            default:
                throw new IllegalArgumentException("Неизвестный статус: " + status);
        }

        return bookings.stream()
                .map(BookingMapper::toResponseDto)
                .collect(Collectors.toList());

    }


    private Booking checkBookingExists(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с id " + bookingId + " не найдено"));
    }

    private User checkUserExists(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
    }

    private Item checkItemExists(long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id " + itemId + " не найдена"));
    }




}
