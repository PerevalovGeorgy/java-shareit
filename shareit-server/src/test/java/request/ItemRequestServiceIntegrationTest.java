package request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.request.*;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = {
        ShareItServer.class,
        ItemRequestRepository.class,
        ItemRequestServiceImpl.class,
        UserRepository.class})
class ItemRequestServiceIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private UserRepository userRepository;

    private ItemRequestService itemRequestService;

    private User user1;
    private User user2;
    private ItemRequest itemRequest;

    @BeforeEach
    void setUp() {
        itemRequestService = new ItemRequestServiceImpl(
                itemRequestRepository,
                userRepository
        );

        user1 = User.builder()
                .name("User1")
                .email("user1@example.com")
                .build();
        entityManager.persist(user1);

        user2 = User.builder()
                .name("User2")
                .email("user2@example.com")
                .build();
        entityManager.persist(user2);

        itemRequest = ItemRequest.builder()
                .description("Need a drill")
                .user(user1)
                .created(LocalDateTime.now())
                .build();
        entityManager.persist(itemRequest);

        Item item = Item.builder()
                .name("Drill")
                .description("Power drill")
                .available(true)
                .owner(user2)
                .request(itemRequest)
                .build();
        entityManager.persist(item);

        entityManager.flush();
    }

    @Test
    void create_ShouldSaveRequestToDatabase() {
        ItemRequestDto dto = ItemRequestDto.builder()
                .description("Need a hammer")
                .build();

        ItemRequestDto saved = itemRequestService.create(user1.getId(), dto);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getDescription()).isEqualTo("Need a hammer");
        assertThat(saved.getRequestorId()).isEqualTo(user1.getId());

        ItemRequest requestFromDb = entityManager.find(ItemRequest.class, saved.getId());
        assertThat(requestFromDb).isNotNull();
        assertThat(requestFromDb.getDescription()).isEqualTo("Need a hammer");
        assertThat(requestFromDb.getUser().getId()).isEqualTo(user1.getId());
    }

    @Test
    @Transactional
    void getAllOtherUsers_ShouldReturnRequestsOfOtherUsers() {
        List<ItemRequestDto> requests = itemRequestService.getAllOtherUsers(user2.getId());

        assertThat(requests).isNotEmpty();
        assertThat(requests.get(0).getRequestorId()).isEqualTo(user1.getId());
    }

    @Test
    void getById_ByOtherUser_ShouldStillReturnRequest() {
        ItemRequestDto found = itemRequestService.getById(user2.getId(), itemRequest.getId());

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(itemRequest.getId());
        assertThat(found.getDescription()).isEqualTo("Need a drill");
        assertThat(found.getRequestorId()).isEqualTo(user1.getId());
    }

    @Test
    void getAll_ShouldReturnAllRequestsSorted() {
        ItemRequest request2 = ItemRequest.builder()
                .description("Need a hammer")
                .user(user2)
                .created(LocalDateTime.now().minusDays(1))
                .build();
        entityManager.persist(request2);
        entityManager.flush();

        List<ItemRequestDto> requests = itemRequestService.getAll();

        assertThat(requests).hasSize(2);
        assertThat(requests.get(0).getDescription()).isEqualTo("Need a drill");
        assertThat(requests.get(1).getDescription()).isEqualTo("Need a hammer");
    }
}