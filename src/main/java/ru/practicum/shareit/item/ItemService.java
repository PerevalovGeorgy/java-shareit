package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collection;

public interface ItemService {

    Collection<ItemDto> findAllByOwner(long userId);

    ItemDto findById(long userId, long id);

    ItemDto create(long userId, ItemDto itemDto);

    ItemDto update(long userId, long itemId, ItemDto itemDto);

    Collection<ItemDto> searchAvailable(String text);

    void delete(long userId, long itemId);
}
