package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.ShareItGateway;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = ShareItGateway.class)
@AutoConfigureMockMvc
@ContextConfiguration(classes = {ShareItGateway.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserClient userClient;

    private UserDto userDto;
    private UpdateUserDto updateUserDto;

    @BeforeEach
    void setUp() {
        userDto = UserDto.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .build();

        updateUserDto = UpdateUserDto.builder()
                .name("Updated User")
                .email("updated@example.com")
                .build();
    }

    @Test
    void create_ShouldReturnCreatedUser() throws Exception {
        UserDto request = UserDto.builder()
                .name("New User")
                .email("new@example.com")
                .build();

        when(userClient.create(any(UserDto.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(userDto));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userClient, times(1)).create(any(UserDto.class));
    }

    @Test
    void create_WithInvalidEmail_ShouldReturnBadRequest() throws Exception {
        UserDto invalidUser = UserDto.builder()
                .name("Test User")
                .email("invalid-email")
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());

        verify(userClient, never()).create(any(UserDto.class));
    }

    @Test
    void update_ShouldReturnUpdatedUser() throws Exception {
        when(userClient.update(anyLong(), any(UpdateUserDto.class)))
                .thenReturn(ResponseEntity.ok(userDto));

        mockMvc.perform(patch("/users/1")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userClient, times(1)).update(eq(1L), any(UpdateUserDto.class));
    }

    @Test
    void update_WithoutHeader_ShouldReturnOk() throws Exception {
        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDto)))
                .andExpect(status().isOk());

        verify(userClient, times(1)).update(eq(1L), any(UpdateUserDto.class));
    }

    @Test
    void get_ShouldReturnUser() throws Exception {
        when(userClient.get(anyLong())).thenReturn(ResponseEntity.ok(userDto));

        mockMvc.perform(get("/users/1")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userClient, times(1)).get(1L);
    }

    @Test
    void getAll_ShouldReturnListOfUsers() throws Exception {
        List<UserDto> users = Arrays.asList(
                UserDto.builder().id(1L).name("User1").email("user1@example.com").build(),
                UserDto.builder().id(2L).name("User2").email("user2@example.com").build()
        );

        when(userClient.getAll()).thenReturn(ResponseEntity.ok(users));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(userClient, times(1)).getAll();
    }

    @Test
    void delete_ShouldReturnNoContent() throws Exception {
        when(userClient.delete(anyLong())).thenReturn(ResponseEntity.noContent().build());

        mockMvc.perform(delete("/users/1")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isNoContent());

        verify(userClient, times(1)).delete(1L);
    }
}