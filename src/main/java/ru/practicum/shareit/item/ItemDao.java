package ru.practicum.shareit.item;


import java.util.Collection;
import java.util.Optional;

public interface ItemDao {
    Collection<Item> findAllByOwner(long ownerId);
    Optional<Item> findById(long id);
    Item create(Item item);
    Item update(Item item);
    void delete(long itemId);
    Collection<Item> searchItems(String text);
}
