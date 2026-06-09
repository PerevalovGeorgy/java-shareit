package ru.practicum.shareit.item.dal.mapper;


import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import javax.swing.tree.RowMapper;

@Component
public class ItemMapper {


    public ItemDto toItemDto(Item item) {
        if (item == null) {
            return null;
        }

        return  ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.isAvailable())
                .ownerId(item.getOwnerId())
                .request(item.getRequest())
                .build();
    }

    public Item toEntity(ItemDto itemDto) {
        if (itemDto == null) {
            return null;
        }

        return Item.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.isAvailable())
                .ownerId(itemDto.getOwnerId())
                .request(itemDto.getRequest())
                .build();
    }
}
