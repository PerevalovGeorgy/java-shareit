package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dal.ItemRepository;
import ru.practicum.shareit.item.dto.NewItemDto;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class InMemoryItemDao implements ItemDao {

    private final Map<Long, Item> items = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Collection<Item> findAllByOwner(long ownerId) {
        log.info("Поиск всех вещей владельца с id: {}", ownerId);

        return items.values().stream()
                .filter(item -> item.getOwnerId() == ownerId)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Item> findById(long id) {
        log.info("Поиск вещи с id: {}", id);
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public Item create(Item item) {
        log.info("Создание новой вещи: {}", item);

        // Валидация
        if (item.getName() == null || item.getName().isBlank()) {
            throw new ValidationException("Название вещи не может быть пустым");
        }
        if (item.getOwnerId() <= 0) {
            throw new ValidationException("Владелец вещи должен быть указан");
        }

        long newId = idGenerator.getAndIncrement();
        item.setId(newId);

        items.put(newId, item);
        log.info("Вещь создана с id: {}", newId);

        return item;
    }

    @Override
    public Item update(Item updatedItem) {
        log.info("Обновление вещи: {}", updatedItem);

        Item existingItem = items.get(updatedItem.getId());
        if (existingItem == null) {
            throw new NotFoundException("Вещь с id " + updatedItem.getId() + " не найдена");
        }

        // Проверка прав: обновлять может только владелец
        if (existingItem.getOwnerId() != updatedItem.getOwnerId()) {
            throw new ValidationException("Редактировать вещь может только владелец");
        }

        // Обновляем только не-null поля
        if (updatedItem.getName() != null) {
            existingItem.setName(updatedItem.getName());
        }
        if (updatedItem.getDescription() != null) {
            existingItem.setDescription(updatedItem.getDescription());
        }
        // available - примитив, поэтому проверяем через булевый флаг
        existingItem.setAvailable(updatedItem.isAvailable());
        if (updatedItem.getRequest() != null) {
            existingItem.setRequest(updatedItem.getRequest());
        }

        items.put(existingItem.getId(), existingItem);
        log.info("Вещь с id {} обновлена", existingItem.getId());

        return existingItem;
    }

    @Override
    public void delete(long id) {
        log.info("Удаление вещи с id: {}", id);

        if (!items.containsKey(id)) {
            throw new NotFoundException("Вещь с id " + id + " не найдена");
        }

        items.remove(id);
        log.info("Вещь с id {} удалена", id);
    }

    @Override
    public Collection<Item> searchItems(String text) {
        return List.of();
    }


    public Collection<Item> searchAvailable(String text) {
        log.info("Поиск доступных вещей по тексту: {}", text);

        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        String lowerText = text.toLowerCase();

        return items.values().stream()
                .filter(Item::isAvailable)  // только доступные
                .filter(item ->
                        (item.getName() != null && item.getName().toLowerCase().contains(lowerText)) ||
                                (item.getDescription() != null && item.getDescription().toLowerCase().contains(lowerText))
                )
                .collect(Collectors.toList());
    }
}