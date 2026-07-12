package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ItemRequestDto create(Long userId, ItemRequestDto itemRequestDto) {
        log.info("Создание запроса на вещь пользователем {}", userId);

        User user = checkUserExists(userId);

        if (itemRequestDto.getDescription() == null || itemRequestDto.getDescription().isBlank()) {
            throw new ValidationException("Описание запроса не может быть пустым");
        }

        ItemRequest itemRequest = ItemRequestMapper.toEntity(itemRequestDto, user);
        ItemRequest savedRequest = itemRequestRepository.save(itemRequest);

        log.info("Запрос на вещь создан с id: {}", savedRequest.getId());
        return ItemRequestMapper.toDto(savedRequest);
    }

    @Override
    public List<ItemRequestDto> getAllByUser(Long userId) {
        log.info("Получение всех запросов пользователя {}", userId);

        User user = checkUserExists(userId);

        List<ItemRequest> requests = itemRequestRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
        List<ItemRequestDto> result = ItemRequestMapper.toDtoWithItemsList(requests);

        log.info("Найдено {} запросов для пользователя {}", result.size(), userId);
        return result;
    }

    @Override
    public List<ItemRequestDto> getAllOtherUsers(Long userId) {
        log.info("Получение всех запросов других пользователей, кроме {}", userId);

        checkUserExists(userId);
        List<ItemRequest> requests = itemRequestRepository.findAllByUserIdNotOrderByCreatedAtDesc(userId);

        return ItemRequestMapper.toDtoWithItemsList(requests);
    }

    @Override
    public ItemRequestDto getById(Long userId, Long requestId) {
        log.info("Получение запроса {} пользователем {}", requestId, userId);

        checkUserExists(userId);
        ItemRequest itemRequest = checkRequestExists(requestId);

        return ItemRequestMapper.toDtoWithItems(itemRequest);
    }

    @Override
    public List<ItemRequestDto> getAll() {
        log.info("Получение всех запросов");

        List<ItemRequest> requests = itemRequestRepository.findAllOrderByCreatedAtDesc();
        return ItemRequestMapper.toDtoWithItemsList(requests);
    }












    private User checkUserExists(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
    }

    private ItemRequest checkRequestExists(Long requestId) {
        return itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с id " + requestId + " не найден"));
    }
}