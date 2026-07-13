package user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.user.*;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = {ShareItServer.class, UserRepository.class, UserServiceImpl.class})
class UserServiceIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository);
    }

    @Test
    void createUser_ShouldSaveUserToDatabase() {
        UserDto userDto = UserDto.builder()
                .name("Test User")
                .email("test@example.com")
                .build();

        UserDto saved = userService.create(userDto);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Test User");
        assertThat(saved.getEmail()).isEqualTo("test@example.com");

        User userFromDb = entityManager.find(User.class, saved.getId());
        assertThat(userFromDb).isNotNull();
        assertThat(userFromDb.getName()).isEqualTo("Test User");
        assertThat(userFromDb.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void updateUser_ShouldUpdateUserInDatabase() {
        User user = User.builder()
                .name("Original Name")
                .email("original@example.com")
                .build();
        entityManager.persist(user);
        entityManager.flush();

        UpdateUserDto updateDto = UpdateUserDto.builder()
                .name("Updated Name")
                .email("updated@example.com")
                .build();

        UserDto updated = userService.update(user.getId(), updateDto);

        assertThat(updated.getName()).isEqualTo("Updated Name");
        assertThat(updated.getEmail()).isEqualTo("updated@example.com");

        User userFromDb = entityManager.find(User.class, user.getId());
        assertThat(userFromDb.getName()).isEqualTo("Updated Name");
        assertThat(userFromDb.getEmail()).isEqualTo("updated@example.com");
    }

    @Test
    void deleteUser_ShouldDeleteUserFromDatabase() {
        User user = User.builder()
                .name("Test User")
                .email("test@example.com")
                .build();
        entityManager.persist(user);
        entityManager.flush();

        userService.delete(user.getId());

        User deletedUser = entityManager.find(User.class, user.getId());
        assertThat(deletedUser).isNull();
    }
}