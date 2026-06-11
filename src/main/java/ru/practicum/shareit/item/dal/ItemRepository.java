package ru.practicum.shareit.item.dal;


import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.dal.mapper.ItemRowMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewItemDto;
import ru.yandex.practicum.filmorate.dal.BaseRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;


@Repository
public class ItemRepository extends BaseRepository<Item> {
    private final JdbcTemplate jdbc;
    protected final ItemRowMapper mapper;

    private static final String ITEM_BASE = "SELECT id, name, description, available, owner_id, request FROM items ";


    public ItemRepository(JdbcTemplate jdbc, ItemRowMapper mapper) {
        super(jdbc, mapper);
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    public Collection<Item> findAll(long userId) {
        String sql = ITEM_BASE + "WHERE owner_id = ?";
        return findManyWithDetails(sql, userId);
    }

    public Optional<Item> findById(long id) {
        String sql = ITEM_BASE + "WHERE f.id = ?";
        return findOneWithDetails(sql, id);
    }

    public Item createItem(Item item) {
        String sql = "INSERT INTO items (name, description, available, owner_id, request) " +
                "VALUES (?, ?, ?, ?, ?)";
        long id = insert(sql,
                item.getName(),
                item.getDescription(),
                item.isAvailable(),
                item.getOwnerId(),
                item.getRequest()
        );
        item.setId(id);
        return item;
    }

    public Item update(long userId, Item item) {
        String sql = "UPDATE items SET name = ?, description = ?, available = ?, request = ? WHERE id = ? AND owner_id = ?";
        jdbc.update(sql,
                item.getName(),
                item.getDescription(),
                item.isAvailable(),
                item.getRequest(),
                item.getId(),
                item.getOwnerId()
        );

        return item;
    }

    public void delete(long itemId) {
        String sql = "DELETE FROM items WHERE id = ?";
        jdbc.update(sql, itemId);
    }

    public Collection<Item> searchItems(String text) {
        String searchPattern = "%" + text.toLowerCase() + "%";
        String sql = ITEM_BASE +
                "WHERE LOWER(name) LIKE ? OR LOWER(description) LIKE ? " +
                "AND available = true";

        return findMany(sql, searchPattern, searchPattern);
    }

    public Collection<Item> findAllByOwner(long ownerId) {
        String sql = ITEM_BASE + "WHERE owner_id = ?";
        return findMany(sql, ownerId);
    }
}
