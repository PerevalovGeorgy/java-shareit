package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dal.mapper.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserDao;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemDao itemDao;
    private final UserDao userDao;
    private final ItemMapper mapper;

    @Override
    public Collection<ItemDto> findAllByOwner(long userId) {
        log.info("Получение всех вещей владельца с id: {}", userId);

        checkUserExists(userId);

        return itemDao.findAllByOwner(userId).stream()
                .map(mapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto findById(long userId, long id) {
        log.info("Получение вещи с id: {} пользователем с id: {}", id, userId);

        checkUserExists(userId);
        Item item = checkItemExists(id);

        return mapper.toItemDto(item);
    }

    @Override
    public ItemDto create(long userId, ItemDto itemDto) {
        log.info("Создание вещи пользователем с id: {}", userId);

        checkUserExists(userId);
        validateName(itemDto);

        Item item = mapper.toEntity(itemDto);
        item.setOwnerId(userId);

        Item created = itemDao.create(item);
        return mapper.toItemDto(created);
    }

    @Override
    public ItemDto update(long userId, long itemId, ItemDto itemDto) {
        log.info("Обновление вещи {} пользователем {}", itemId, userId);

        checkUserExists(userId);

        Item existingItem = checkItemExists(itemId);

        checkOwner(existingItem, userId);

        if (itemDto.getName() != null) {
            existingItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            existingItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
        }
        if (itemDto.getRequest() != null) {
            existingItem.setRequest(itemDto.getRequest());
        }

        Item updated = itemDao.update(existingItem);
        return mapper.toItemDto(updated);
    }

    @Override
    public Collection<ItemDto> searchAvailable(String text) {
        log.info("Поиск доступных вещей по тексту: {}", text);

        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        return itemDao.searchItems(text).stream()
                .map(mapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(long userId, long itemId) {
        log.info("Удаление вещи с id: {} пользователем с id: {}", itemId, userId);

        checkUserExists(userId);
        Item existingItem = checkItemExists(itemId);
        checkOwnerToDelete(existingItem, userId);

        itemDao.delete(itemId);
    }

    private void checkUserExists(long userId) {
        if (!userDao.existsById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
    }

    private void checkOwner(Item existingItem, long userId) {
        if (existingItem.getOwnerId() != userId) {
            throw new AccessDeniedException("Редактировать вещь может только владелец");
        }
    }

    private void checkOwnerToDelete(Item existingItem, long userId) {
        if (existingItem.getOwnerId() != userId) {
            throw new AccessDeniedException("Удалить вещь может только её владелец");
        }
    }

    private void validateName(ItemDto itemDto) {
        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new ValidationException("Название вещи не может быть пустым");
        }
    }

    private Item checkItemExists(long id) {
        Optional<Item> optionalItem = itemDao.findById(id);
        if (optionalItem.isEmpty()) {
            throw new NotFoundException("Вещь с id " + id + " не найдена");
        }
        return optionalItem.get();
    }
}
