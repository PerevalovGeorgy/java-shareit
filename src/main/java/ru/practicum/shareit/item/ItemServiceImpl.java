package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;
    private final ItemRequestRepository itemRequestRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;


    @Override
    public Collection<ItemDto> findAllByOwner(long userId) {
        log.info("Получение всех вещей владельца с id: {}", userId);

        checkUserExists(userId);
        List<Item> items = itemRepository.findByOwnerId(userId);

        if (items.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .collect(Collectors.toList());

        List<Booking> allBookings = bookingRepository.findAllApprovedBookingsByItemIds(itemIds);

        Map<Long, List<Booking>> bookingsByItem = allBookings.stream()
                .collect(Collectors.groupingBy(b -> b.getItem().getId()));

        List<Comment> allComments = commentRepository.findAllByItemIds(itemIds);
        Map<Long, List<Comment>> commentsByItem = allComments.stream()
                .collect(Collectors.groupingBy(c -> c.getItem().getId()));

        LocalDateTime now = LocalDateTime.now();
        List<ItemDto> result = new ArrayList<>();

        for (Item item : items) {
            List<Booking> itemBookings = bookingsByItem.getOrDefault(item.getId(), Collections.emptyList());

            Booking lastBooking = itemBookings.stream()
                    .filter(b -> b.getEnd().isBefore(now))
                    .findFirst()
                    .orElse(null);

            Booking nextBooking = itemBookings.stream()
                    .filter(b -> b.getStart().isAfter(now))
                    .reduce((first, second) -> first)
                    .orElse(null);

            List<Comment> itemComments = commentsByItem.getOrDefault(item.getId(), Collections.emptyList());

            ItemDto itemDto = itemMapper.toItemDtoWithBookings(
                    item, lastBooking, nextBooking, itemComments);
            result.add(itemDto);
        }

        return result;
    }

    @Override
    public ItemDto findById(long userId, long itemId) {
        log.info("Получение вещи с id: {} пользователем с id: {}", itemId, userId);

        checkUserExists(userId);
        Item item = checkItemExists(itemId);

        List<Comment> comments = commentRepository.findAllByItemId(itemId);
        List<CommentDto> commentDtos = commentMapper.toCommentDtoList(comments);

        if (item.getOwner().getId().equals(userId)) {
            LocalDateTime now = LocalDateTime.now();

            List<Booking> lastBookings = bookingRepository.findLastBookingsByItemId(itemId, now);
            Booking lastBooking = lastBookings.isEmpty() ? null : lastBookings.get(0);

            List<Booking> nextBookings = bookingRepository.findNextBookingsByItemId(itemId, now);
            Booking nextBooking = nextBookings.isEmpty() ? null : nextBookings.get(0);

            return itemMapper.toItemDtoWithBookings(item, lastBooking, nextBooking, comments);
        } else {
            ItemDto itemDto = itemMapper.toItemDto(item);
            itemDto.setComments(commentDtos);
            return itemDto;
        }
    }

    @Override
    @Transactional
    public CommentDto addComment(long userId, long itemId, CommentDto commentDto) {
        log.info("Добавление комментария к вещи {} пользователем {}", itemId, userId);

        User author = checkUserExists(userId);

        Item item = checkItemExists(itemId);

        if (item.getOwner().getId().equals(userId)) {
            throw new ValidationException("Владелец не может оставлять комментарий к своей вещи");
        }

        LocalDateTime now = LocalDateTime.now();
        boolean hasBooked = bookingRepository.existsByItemIdAndUserIdAndStatusApprovedAndEndBefore(
                itemId, userId, now);

        if (!hasBooked) {
            throw new ValidationException("Пользователь не может оставить комментарий, так как не брал вещь в аренду или аренда ещё не завершена");
        }

        if (commentDto.getText() == null || commentDto.getText().isBlank()) {
            throw new ValidationException("Текст комментария не может быть пустым");
        }

        Comment comment = commentMapper.toEntity(commentDto, item, author);
        Comment savedComment = commentRepository.save(comment);

        log.info("Комментарий добавлен с id: {} к вещи {}", savedComment.getId(), itemId);
        return commentMapper.toCommentDto(savedComment);
    }

    @Override
    @Transactional
    public ItemDto create(long userId, ItemDto itemDto) {
        log.info("Создание вещи пользователем с id: {}", userId);

        User owner = checkUserExists(userId);
        validateItemDto(itemDto);

        ItemRequest itemRequest = null;
        if (itemDto.getRequestId() != null) {
            itemRequest = itemRequestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Запрос с id " + itemDto.getRequestId() + " не найден"));
        }

        Item item = itemMapper.toEntity(itemDto, owner, itemRequest);
        Item created = itemRepository.save(item);

        log.info("Успешное создание вещи с id: {} пользователем с id: {}", created.getId(), userId);
        return itemMapper.toItemDto(created);
    }

    @Override
    @Transactional
    public ItemDto update(long userId, long itemId, ItemDto itemDto) {
        log.info("Обновление вещи {} пользователем {}", itemId, userId);

        checkUserExists(userId);
        Item existingItem = checkItemExists(itemId);
        checkOwner(existingItem, userId);

        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            existingItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            existingItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
        }
        if (itemDto.getRequestId() != null) {
            ItemRequest request = itemRequestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Запрос с id " + itemDto.getRequestId() + " не найден"));
            existingItem.setRequest(request);
        }

        Item updated = itemRepository.save(existingItem);
        log.info("Успешное обновление вещи {} пользователем {}", itemId, userId);
        return itemMapper.toItemDto(updated);
    }

    @Override
    public Collection<ItemDto> searchAvailable(String text) {
        log.info("Поиск доступных вещей по тексту: {}", text);

        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        return itemRepository.searchAvailable(text).stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(long userId, long itemId) {
        log.info("Удаление вещи с id: {} пользователем с id: {}", itemId, userId);

        checkUserExists(userId);
        Item existingItem = checkItemExists(itemId);
        checkOwner(existingItem, userId);

        itemRepository.delete(existingItem);
        log.info("Успешное удаление вещи с id: {} пользователем с id: {}", itemId, userId);
    }

    private User checkUserExists(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
    }

    private void checkOwner(Item item, long userId) {
        if (!item.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Редактировать вещь может только владелец");
        }
    }

    private void validateItemDto(ItemDto itemDto) {
        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new ValidationException("Название вещи не может быть пустым");
        }

        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            throw new ValidationException("Описание вещи не может быть пустым");
        }

        if (itemDto.getAvailable() == null) {
            throw new ValidationException("Доступность вещи должна быть указана");
        }
    }

    private Item checkItemExists(long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id " + itemId + " не найдена"));
    }
}
