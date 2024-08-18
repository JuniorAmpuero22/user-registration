package com.example.user_registration.service;

import com.example.user_registration.dto.PhoneDto;
import com.example.user_registration.dto.UserDto;
import com.example.user_registration.dto.UserResponse;
import com.example.user_registration.model.Phone;
import com.example.user_registration.model.User;
import com.example.user_registration.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User existingUser;

    private UserDto userDto;

    private UUID userId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

    }
    public static final String MESSAGE_EMAIL_ALREADY_REGISTERED = "El correo ya registrado";
    public static final String MESSAGE_USER_NOT_FOUND = "Usuario no encontrado";
    public static final String ERROR_EXPECTED_EXCEPTION = "El método deleteUser no lanzó la excepción esperada al intentar eliminar un usuario que no existe.";

    @Test
    void testCreateUser_success() {
        // Datos de entrada
        UserDto userDto = new UserDto();
        userDto.setName("John Doe");
        userDto.setEmail("john.doe@example.com");
        userDto.setPassword("password123");
        PhoneDto phoneDto = new PhoneDto("123456789", "01", "51");
        userDto.setPhones(Collections.singletonList(phoneDto));

        // Mock del repositorio
        when(userRepository.existsByEmail(userDto.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });

        // Llamada al método
        UserResponse response = userService.createUser(userDto);

        // Validaciones
        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getToken()).isNotNull();
        assertThat(response.getCreated()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(response.isActive()).isTrue();

        // Verificar interacciones
        verify(userRepository, times(1)).existsByEmail(userDto.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testCreateUser_emailAlreadyRegistered() {
        // Datos de entrada
        UserDto userDto = new UserDto();
        userDto.setEmail("john.doe@example.com");

        // Mock del repositorio
        when(userRepository.existsByEmail(userDto.getEmail())).thenReturn(true);

        // Llamada al método y validación de excepción
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(userDto);
        });

        assertThat(exception.getMessage()).isEqualTo(MESSAGE_EMAIL_ALREADY_REGISTERED);

        // Verificar interacciones
        verify(userRepository, times(1)).existsByEmail(userDto.getEmail());
        verify(userRepository, times(0)).save(any(User.class));
    }

    @Test
    void updateUser_ShouldUpdateUserDetails() {
        // Setup
        UUID userId = UUID.randomUUID();
        UserDto userDto = new UserDto();
        userDto.setName("Updated Name");
        userDto.setEmail("updated@example.com");
        userDto.setPassword("newPassword");
        userDto.setPhones(Arrays.asList(new PhoneDto("1234", "5678", "55")));

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setName("Old Name");
        existingUser.setEmail("old@example.com");
        existingUser.setPassword("oldPassword");
        existingUser.setPhones(new ArrayList<>()); // Asegúrate de inicializar la lista de teléfonos
        Phone phone = new Phone();
        phone.setNumber("33");
        phone.setCitycode("4444");
        phone.setCountrycode("44");
        existingUser.getPhones().add(phone);
        existingUser.setCreated(LocalDateTime.now().minusDays(1));
        existingUser.setModified(LocalDateTime.now().minusDays(1));
        existingUser.setLastLogin(LocalDateTime.now().minusDays(1));
        existingUser.setToken("oldToken");
        existingUser.setActive(true);

        UserResponse expectedResponse = new UserResponse(
                userId,
                existingUser.getCreated(),
                LocalDateTime.now(),
                existingUser.getLastLogin(),
                existingUser.getToken(),
                existingUser.isActive()
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArguments()[0]);

        // Act
        UserResponse response = userService.updateUser(userId, userDto);

        // Assert
        assertNotNull(response);
        assertEquals(userId, response.getId());
        assertEquals(1, existingUser.getPhones().size());
        assertEquals("1234", existingUser.getPhones().get(0).getNumber());
        assertEquals("5678", existingUser.getPhones().get(0).getCitycode());
        assertEquals("55", existingUser.getPhones().get(0).getCountrycode());
        assertEquals(expectedResponse.getCreated(), response.getCreated());
        assertEquals(expectedResponse.getLastLogin(), response.getLastLogin());
        assertEquals(expectedResponse.getToken(), response.getToken());
        assertEquals(expectedResponse.isActive(), response.isActive());
    }

    @Test
    void deleteUser_ShouldThrowException_WhenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();
        when(userRepository.existsById(userId)).thenReturn(false);
        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> userService.deleteUser(userId),
                ERROR_EXPECTED_EXCEPTION
        );
        assertEquals(MESSAGE_USER_NOT_FOUND, thrown.getMessage());
    }

    @Test
    void deleteUser_ShouldDeleteUser_WhenUserExists() {
        UUID userId = UUID.randomUUID();
        when(userRepository.existsById(userId)).thenReturn(true);
        userService.deleteUser(userId);
        verify(userRepository, times(1)).deleteById(userId);
    }
}
