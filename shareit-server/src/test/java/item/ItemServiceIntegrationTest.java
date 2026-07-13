package item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.item.*;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.ItemRequestServiceImpl;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = {
        ShareItServer.class,
        ItemRepository.class,
        UserRepository.class,
        BookingRepository.class,
        CommentRepository.class,
        ItemServiceImpl.class})
class ItemServiceIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ItemService itemService;

    private User owner;
    private User booker;
    private Item item;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .name("Owner")
                .email("owner@example.com")
                .build();
        entityManager.persist(owner);

        booker = User.builder()
                .name("Booker")
                .email("booker@example.com")
                .build();
        entityManager.persist(booker);

        item = Item.builder()
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .owner(owner)
                .build();
        entityManager.persist(item);

        entityManager.flush();
    }

    @Test
    void create_ShouldSaveItemToDatabase() {
        ItemDto itemDto = ItemDto.builder()
                .name("New Item")
                .description("New Description")
                .available(true)
                .build();

        ItemDto saved = itemService.create(owner.getId(), itemDto);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("New Item");
        assertThat(saved.getDescription()).isEqualTo("New Description");
        assertThat(saved.getAvailable()).isTrue();

        Item itemFromDb = entityManager.find(Item.class, saved.getId());
        assertThat(itemFromDb).isNotNull();
        assertThat(itemFromDb.getName()).isEqualTo("New Item");
        assertThat(itemFromDb.getOwner().getId()).isEqualTo(owner.getId());
    }

    @Test
    void findById_ShouldReturnItemWithComments() {
        Comment comment = Comment.builder()
                .text("Great item!")
                .item(item)
                .user(booker)
                .created(LocalDateTime.now())
                .build();
        entityManager.persist(comment);
        entityManager.flush();

        ItemDto found = itemService.findById(booker.getId(), item.getId());

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(item.getId());
        assertThat(found.getComments()).isNotEmpty();
        assertThat(found.getComments().get(0).getText()).isEqualTo("Great item!");
    }

    @Test
    void update_ShouldUpdateItemInDatabase() {
        ItemDto updateDto = ItemDto.builder()
                .name("Updated Name")
                .description("Updated Description")
                .available(false)
                .build();

        ItemDto updated = itemService.update(owner.getId(), item.getId(), updateDto);

        assertThat(updated.getName()).isEqualTo("Updated Name");
        assertThat(updated.getDescription()).isEqualTo("Updated Description");
        assertThat(updated.getAvailable()).isFalse();

        Item itemFromDb = entityManager.find(Item.class, item.getId());
        assertThat(itemFromDb.getName()).isEqualTo("Updated Name");
        assertThat(itemFromDb.getDescription()).isEqualTo("Updated Description");
        assertThat(itemFromDb.isAvailable()).isFalse();
    }

    @Test
    void delete_ShouldDeleteItemFromDatabase() {
        itemService.delete(owner.getId(), item.getId());

        Item deletedItem = entityManager.find(Item.class, item.getId());
        assertThat(deletedItem).isNull();
    }

    @Test
    void searchAvailable_ShouldReturnOnlyAvailableItems() {
        Item unavailableItem = Item.builder()
                .name("Unavailable Item")
                .description("Unavailable Description")
                .available(false)
                .owner(owner)
                .build();
        entityManager.persist(unavailableItem);
        entityManager.flush();

        Collection<ItemDto> results = itemService.searchAvailable("Item");

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(item -> item.getAvailable());
    }

    @Test
    void addComment_ShouldSaveCommentToDatabase() {
        Booking booking = Booking.builder()
                .item(item)
                .booker(booker)
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .status(Status.APPROVED)
                .build();
        entityManager.persist(booking);
        entityManager.flush();

        CommentTextDto commentText = CommentTextDto.builder()
                .text("Great item!")
                .build();

        CommentDto saved = itemService.addComment(booker.getId(), item.getId(), commentText);

        assertThat(saved).isNotNull();
        assertThat(saved.getText()).isEqualTo("Great item!");
        assertThat(saved.getAuthorName()).isEqualTo(booker.getName());

        Comment commentFromDb = entityManager.find(Comment.class, saved.getId());
        assertThat(commentFromDb).isNotNull();
        assertThat(commentFromDb.getText()).isEqualTo("Great item!");
        assertThat(commentFromDb.getUser().getId()).isEqualTo(booker.getId());
    }
}