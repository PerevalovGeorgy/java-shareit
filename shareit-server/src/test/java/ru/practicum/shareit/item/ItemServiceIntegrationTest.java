package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import ru.practicum.shareit.ShareitServer;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = {
        ShareitServer.class,
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

    @Test
    void create_WhenUserNotFound_ShouldThrowNotFoundException() {
        ItemDto itemDto = ItemDto.builder()
                .name("New Item")
                .description("New Description")
                .available(true)
                .build();

        assertThrows(NotFoundException.class, () -> {
            itemService.create(999L, itemDto);
        });
    }

    @Test
    void create_WhenNameIsBlank_ShouldThrowValidationException() {
        ItemDto itemDto = ItemDto.builder()
                .name("")
                .description("New Description")
                .available(true)
                .build();

        assertThrows(ValidationException.class, () -> {
            itemService.create(owner.getId(), itemDto);
        });
    }

    @Test
    void create_WhenDescriptionIsBlank_ShouldThrowValidationException() {
        ItemDto itemDto = ItemDto.builder()
                .name("New Item")
                .description("")
                .available(true)
                .build();

        assertThrows(ValidationException.class, () -> {
            itemService.create(owner.getId(), itemDto);
        });
    }

    @Test
    void create_WhenAvailableIsNull_ShouldThrowValidationException() {
        ItemDto itemDto = ItemDto.builder()
                .name("New Item")
                .description("New Description")
                .available(null)
                .build();

        assertThrows(ValidationException.class, () -> {
            itemService.create(owner.getId(), itemDto);
        });
    }

    @Test
    void findById_WhenUserNotFound_ShouldThrowNotFoundException() {
        assertThrows(NotFoundException.class, () -> {
            itemService.findById(999L, item.getId());
        });
    }

    @Test
    void findById_WhenItemNotFound_ShouldThrowNotFoundException() {
        assertThrows(NotFoundException.class, () -> {
            itemService.findById(booker.getId(), 999L);
        });
    }

    @Test
    void findById_WhenOwnerRequests_ShouldReturnItemWithBookings() {
        Booking booking = Booking.builder()
                .item(item)
                .booker(booker)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(Status.APPROVED)
                .createdAt(LocalDateTime.now())
                .build();
        entityManager.persist(booking);
        entityManager.flush();

        ItemDto found = itemService.findById(owner.getId(), item.getId());

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(item.getId());
    }

    @Test
    void update_WhenUserNotFound_ShouldThrowNotFoundException() {
        ItemDto updateDto = ItemDto.builder()
                .name("Updated Name")
                .build();

        assertThrows(NotFoundException.class, () -> {
            itemService.update(999L, item.getId(), updateDto);
        });
    }

    @Test
    void update_WhenItemNotFound_ShouldThrowNotFoundException() {
        ItemDto updateDto = ItemDto.builder()
                .name("Updated Name")
                .build();

        assertThrows(NotFoundException.class, () -> {
            itemService.update(owner.getId(), 999L, updateDto);
        });
    }

    @Test
    void update_WhenNotOwner_ShouldThrowAccessDeniedException() {
        ItemDto updateDto = ItemDto.builder()
                .name("Updated Name")
                .build();

        assertThrows(AccessDeniedException.class, () -> {
            itemService.update(booker.getId(), item.getId(), updateDto);
        });
    }

    @Test
    void update_WithNullFields_ShouldKeepExistingValues() {
        ItemDto updateDto = ItemDto.builder()
                .name(null)
                .description(null)
                .available(null)
                .build();

        ItemDto updated = itemService.update(owner.getId(), item.getId(), updateDto);

        assertThat(updated.getName()).isEqualTo("Test Item");
        assertThat(updated.getDescription()).isEqualTo("Test Description");
        assertThat(updated.getAvailable()).isTrue();
    }

    @Test
    void update_WithOnlyName_ShouldUpdateOnlyName() {
        String newName = "Only Name Updated";
        ItemDto updateDto = ItemDto.builder()
                .name(newName)
                .build();

        ItemDto updated = itemService.update(owner.getId(), item.getId(), updateDto);

        assertThat(updated.getName()).isEqualTo(newName);
        assertThat(updated.getDescription()).isEqualTo("Test Description");
        assertThat(updated.getAvailable()).isTrue();
    }

    @Test
    void update_WithOnlyDescription_ShouldUpdateOnlyDescription() {
        String newDescription = "Only Description Updated";
        ItemDto updateDto = ItemDto.builder()
                .description(newDescription)
                .build();

        ItemDto updated = itemService.update(owner.getId(), item.getId(), updateDto);

        assertThat(updated.getName()).isEqualTo("Test Item");
        assertThat(updated.getDescription()).isEqualTo(newDescription);
        assertThat(updated.getAvailable()).isTrue();
    }

    @Test
    void update_WithOnlyAvailable_ShouldUpdateOnlyAvailable() {
        ItemDto updateDto = ItemDto.builder()
                .available(false)
                .build();

        ItemDto updated = itemService.update(owner.getId(), item.getId(), updateDto);

        assertThat(updated.getName()).isEqualTo("Test Item");
        assertThat(updated.getDescription()).isEqualTo("Test Description");
        assertThat(updated.getAvailable()).isFalse();
    }

    @Test
    void findAllByOwner_WhenUserNotFound_ShouldThrowNotFoundException() {
        assertThrows(NotFoundException.class, () -> {
            itemService.findAllByOwner(999L);
        });
    }

    @Test
    void findAllByOwner_WithMultipleItems_ShouldReturnAllItems() {
        Item item2 = Item.builder()
                .name("Second Item")
                .description("Second Description")
                .available(true)
                .owner(owner)
                .build();
        entityManager.persist(item2);
        entityManager.flush();

        Collection<ItemDto> items = itemService.findAllByOwner(owner.getId());

        assertThat(items).hasSize(2);
        assertThat(items).extracting(ItemDto::getName)
                .contains("Test Item", "Second Item");
    }

    @Test
    void searchAvailable_WithNullText_ShouldReturnEmptyList() {
        Collection<ItemDto> results = itemService.searchAvailable(null);

        assertThat(results).isEmpty();
    }

    @Test
    void searchAvailable_WithBlankText_ShouldReturnEmptyList() {
        Collection<ItemDto> results = itemService.searchAvailable("   ");

        assertThat(results).isEmpty();
    }

    @Test
    void searchAvailable_WithNoMatchingText_ShouldReturnEmptyList() {
        Collection<ItemDto> results = itemService.searchAvailable("Nonexistent");

        assertThat(results).isEmpty();
    }

    @Test
    void searchAvailable_WithMatchingText_ShouldReturnItems() {
        Item item2 = Item.builder()
                .name("Drill")
                .description("Power drill for wood")
                .available(true)
                .owner(owner)
                .build();
        entityManager.persist(item2);
        entityManager.flush();

        Collection<ItemDto> results = itemService.searchAvailable("Drill");

        assertThat(results).hasSize(1);
        assertThat(results.iterator().next().getName()).isEqualTo("Drill");
    }

    @Test
    void searchAvailable_ShouldNotReturnUnavailableItems() {
        Item unavailableItem = Item.builder()
                .name("Unavailable Drill")
                .description("Not available")
                .available(false)
                .owner(owner)
                .build();
        entityManager.persist(unavailableItem);
        entityManager.flush();

        Collection<ItemDto> results = itemService.searchAvailable("Drill");

        assertThat(results).isEmpty();
    }

    @Test
    void delete_WhenUserNotFound_ShouldThrowNotFoundException() {
        assertThrows(NotFoundException.class, () -> {
            itemService.delete(999L, item.getId());
        });
    }

    @Test
    void delete_WhenItemNotFound_ShouldThrowNotFoundException() {
        assertThrows(NotFoundException.class, () -> {
            itemService.delete(owner.getId(), 999L);
        });
    }

    @Test
    void delete_WhenNotOwner_ShouldThrowAccessDeniedException() {
        assertThrows(AccessDeniedException.class, () -> {
            itemService.delete(booker.getId(), item.getId());
        });
    }

    @Test
    void addComment_WhenUserNotFound_ShouldThrowNotFoundException() {
        CommentTextDto commentText = CommentTextDto.builder()
                .text("Great item!")
                .build();

        assertThrows(NotFoundException.class, () -> {
            itemService.addComment(999L, item.getId(), commentText);
        });
    }

    @Test
    void addComment_WhenItemNotFound_ShouldThrowNotFoundException() {
        CommentTextDto commentText = CommentTextDto.builder()
                .text("Great item!")
                .build();

        assertThrows(NotFoundException.class, () -> {
            itemService.addComment(booker.getId(), 999L, commentText);
        });
    }

    @Test
    void addComment_WhenNoApprovedBooking_ShouldThrowValidationException() {
        CommentTextDto commentText = CommentTextDto.builder()
                .text("Great item!")
                .build();

        assertThrows(ValidationException.class, () -> {
            itemService.addComment(booker.getId(), item.getId(), commentText);
        });
    }

    @Test
    void addComment_WhenBookingNotFinishedAndRecent_ShouldThrowValidationException() {
        Booking booking = Booking.builder()
                .item(item)
                .booker(booker)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(Status.APPROVED)
                .createdAt(LocalDateTime.now())
                .build();
        entityManager.persist(booking);
        entityManager.flush();

        CommentTextDto commentText = CommentTextDto.builder()
                .text("Great item!")
                .build();

        assertThrows(ValidationException.class, () -> {
            itemService.addComment(booker.getId(), item.getId(), commentText);
        });
    }

    @Test
    void addComment_WhenBookingFinished_ShouldSaveComment() {
        Booking booking = Booking.builder()
                .item(item)
                .booker(booker)
                .start(LocalDateTime.now().minusDays(3))
                .end(LocalDateTime.now().minusDays(1))
                .status(Status.APPROVED)
                .createdAt(LocalDateTime.now().minusDays(4))
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
    }
}