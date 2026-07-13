package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.exception.*;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = ShareItServer.class)
@AutoConfigureMockMvc
@ContextConfiguration(classes = {ShareItServer.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

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

        UserDto response = UserDto.builder()
                .id(1L)
                .name(request.getName())
                .email(request.getEmail())
                .build();

        when(userService.create(any(UserDto.class))).thenReturn(response);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("New User"))
                .andExpect(jsonPath("$.email").value("new@example.com"));

        verify(userService, times(1)).create(any(UserDto.class));
    }

    @Test
    void update_ShouldReturnUpdatedUser() throws Exception {
        UpdateUserDto request = UpdateUserDto.builder()
                .name("Updated Name")
                .email("updated@example.com")
                .build();

        UserDto response = UserDto.builder()
                .id(1L)
                .name(request.getName())
                .email(request.getEmail())
                .build();

        when(userService.update(anyLong(), any(UpdateUserDto.class))).thenReturn(response);

        mockMvc.perform(patch("/users/1")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));

        verify(userService, times(1)).update(eq(1L), any(UpdateUserDto.class));
    }

    @Test
    void get_ShouldReturnUser() throws Exception {
        when(userService.get(anyLong())).thenReturn(userDto);

        mockMvc.perform(get("/users/1")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService, times(1)).get(1L);
    }


    @Test
    void get_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        when(userService.get(anyLong())).thenThrow(new NotFoundException("User not found"));

        mockMvc.perform(get("/users/999")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).get(999L);
    }

    @Test
    void getAll_ShouldReturnListOfUsers() throws Exception {
        List<UserDto> users = Arrays.asList(
                UserDto.builder().id(1L).name("User1").email("user1@example.com").build(),
                UserDto.builder().id(2L).name("User2").email("user2@example.com").build()
        );

        when(userService.getAll()).thenReturn(users);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[0].name").value("User1"))
                .andExpect(jsonPath("$[1].name").value("User2"));

        verify(userService, times(1)).getAll();
    }

    @Test
    void getAll_WithNoUsers_ShouldReturnEmptyList() throws Exception {
        when(userService.getAll()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(userService, times(1)).getAll();
    }

    @Test
    void delete_ShouldReturnNoContent() throws Exception {
        doNothing().when(userService).delete(anyLong());

        mockMvc.perform(delete("/users/1")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).delete(1L);
    }

    @Test
    void getByEmail_ShouldReturnUser() throws Exception {
        when(userService.getByEmail(anyString())).thenReturn(userDto);

        mockMvc.perform(get("/users/search")
                        .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService, times(1)).getByEmail("test@example.com");
    }

    @Test
    void getByEmail_WithNonExistentEmail_ShouldReturnNotFound() throws Exception {
        when(userService.getByEmail(anyString())).thenThrow(new NotFoundException("User not found"));

        mockMvc.perform(get("/users/search")
                        .param("email", "nonexistent@example.com"))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getByEmail("nonexistent@example.com");
    }
}