package ru.practicum.shareit.item;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.Booking;
import org.springframework.context.annotation.Lazy;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ItemMapper {

    private final BookingMapper bookingMapper;
    private final CommentMapper commentMapper;

    public ItemMapper(@Lazy BookingMapper bookingMapper, CommentMapper commentMapper) {
        this.bookingMapper = bookingMapper;
        this.commentMapper = commentMapper;
    }

    public ItemDto toItemDto(Item item) {
        if (item == null) {
            return null;
        }

        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.isAvailable())
                .ownerId(item.getOwner() != null ? item.getOwner().getId() : null)
                .requestId(item.getRequest() != null ? item.getRequest().getId() : null)
                .build();
    }

    public ItemDto toItemDtoWithBookings(Item item, Booking lastBooking, Booking nextBooking, List<Comment> comments) {
        if (item == null) {
            return null;
        }

        ItemDto.ItemDtoBuilder builder = ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.isAvailable())
                .ownerId(item.getOwner() != null ? item.getOwner().getId() : null)
                .requestId(item.getRequest() != null ? item.getRequest().getId() : null);

        if (lastBooking != null) {
            builder.lastBooking(bookingMapper.toShortDto(lastBooking));
        }

        if (nextBooking != null) {
            builder.nextBooking(bookingMapper.toShortDto(nextBooking));
        }

        if (comments != null && !comments.isEmpty()) {
            builder.comments(comments.stream()
                    .map(commentMapper::toCommentDto)
                    .collect(Collectors.toList()));
        }

        return builder.build();
    }

    public Item toEntity(ItemDto itemDto, User owner, ItemRequest request) {
        if (itemDto == null) {
            return null;
        }

        return Item.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable() != null && itemDto.getAvailable())
                .owner(owner)
                .request(request)
                .build();
    }

    public void updateItemFromDto(ItemDto itemDto, Item item) {
        if (itemDto == null || item == null) {
            return;
        }

        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            item.setName(itemDto.getName());
        }

        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            item.setDescription(itemDto.getDescription());
        }

        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
    }

    public CommentDto toCommentDto(Comment comment) {
        if (comment == null) {
            return null;
        }

        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getUser() != null ? comment.getUser().getName() : null)
                .created(comment.getCreated())
                .build();
    }
}