package ru.practicum.shareit.request;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto create(Long userId, ItemRequestDto itemRequestDto);
    List<ItemRequestDto> getAllByUser(Long userId);
    List<ItemRequestDto> getAllOtherUsers(Long userId);
    ItemRequestDto getById(Long userId, Long requestId);
    List<ItemRequestDto> getAll();
}
