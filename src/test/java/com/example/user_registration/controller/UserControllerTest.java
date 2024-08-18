package com.example.user_registration.controller;

import com.example.user_registration.dto.PhoneDto;
import com.example.user_registration.dto.UserDto;
import com.example.user_registration.dto.UserResponse;
import com.example.user_registration.service.UserService;
import com.example.user_registration.util.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.UUID;

import static com.example.user_registration.util.constantes.MESSAGE_USER_DELETED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(GlobalExceptionHandler.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerUser_ShouldReturnCreatedUser() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setName("John Doe");
        userDto.setEmail("john@example.com");
        userDto.setPassword("Password123@");
        userDto.setPhones(Arrays.asList(new PhoneDto("1234", "5678", "55")));

        UserResponse userResponse = new UserResponse(
                UUID.randomUUID(), // id
                LocalDateTime.now(), // created
                LocalDateTime.now(), // modified
                LocalDateTime.now(), // lastLogin
                "token", // token
                true // isActive
        );

        when(userService.createUser(userDto)).thenReturn(userResponse);

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.token").value("token"));
    }
    @Test
    void updateUser_ShouldReturnUpdatedUser() throws Exception {
        // Arrange
        UUID userId = UUID.fromString("13bd02ad-5110-4340-80cb-0b5efa0a8166");
        UserDto userDto = new UserDto();
        userDto.setName("Updated Name");
        userDto.setEmail("updated@example.com");
        userDto.setPassword("newPassword");
        userDto.setPhones(Arrays.asList(new PhoneDto("1234", "5678", "55")));

        UserResponse userResponse = new UserResponse(
                UUID.randomUUID(), // id
                LocalDateTime.now(), // created
                LocalDateTime.now(), // modified
                LocalDateTime.now(), // lastLogin
                "token", // token
                true // isActive
        );

        when(userService.updateUser(eq(userId), any(UserDto.class))).thenReturn(userResponse);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String nowFormatted = LocalDateTime.now().format(formatter);
        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.put("/api/users/" + userId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(userResponse.getId().toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.created").value(nowFormatted))
                .andExpect(MockMvcResultMatchers.jsonPath("$.modified").value(nowFormatted))
                .andExpect(MockMvcResultMatchers.jsonPath("$.token").value("token"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.active").value(true));
    }

    @Test
    void deleteUser_ShouldReturnSuccessMessage() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(delete("/api/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(content().string(MESSAGE_USER_DELETED));
    }

}
