package com.example.user_registration.service;

import com.example.user_registration.dto.PhoneDto;
import com.example.user_registration.dto.UserDto;
import com.example.user_registration.dto.UserResponse;
import com.example.user_registration.model.Phone;
import com.example.user_registration.model.User;
import com.example.user_registration.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public UserResponse createUser(UserDto userDto) {
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new IllegalArgumentException("El correo ya registrado");
        }

        // Mapear UserDto a User
        User user = new User();
        user.setId(UUID.fromString(UUID.randomUUID().toString()));
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setPassword(userDto.getPassword());
        user.setCreated(LocalDateTime.now());
        user.setModified(LocalDateTime.now());
        user.setLastLogin(LocalDateTime.now());
        user.setToken(UUID.randomUUID().toString());
        user.setActive(true);

        if (userDto.getPhones() != null) {
            List<Phone> phones = userDto.getPhones().stream().map(phoneDto -> {
                Phone phone = new Phone();
                phone.setNumber(phoneDto.getNumber());
                phone.setCitycode(phoneDto.getCitycode());
                phone.setCountrycode(phoneDto.getCountrycode());
                phone.setUser(user);
                return phone;
            }).collect(Collectors.toList());
            user.setPhones(phones);
        }

        User savedUser = userRepository.save(user);

        return new UserResponse(
                savedUser.getId(),
                savedUser.getCreated(),
                savedUser.getModified(),
                savedUser.getLastLogin(),
                savedUser.getToken(),
                savedUser.isActive()
        );
    }

    @Transactional
    public UserResponse updateUser(UUID userId, UserDto userDto) {
        // Buscar el usuario existente por ID
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Actualizar campos básicos del usuario
        existingUser.setName(userDto.getName());
        existingUser.setEmail(userDto.getEmail());
        existingUser.setPassword(userDto.getPassword());
        existingUser.setModified(LocalDateTime.now());

        // Manejo de la lista de teléfonos
        updatePhoneList(existingUser, userDto.getPhones());

        // Guardar el usuario actualizado en la base de datos
        User savedUser = userRepository.save(existingUser);

        // Crear y retornar la respuesta con los datos correctos
        return new UserResponse(
                savedUser.getId(),
                savedUser.getCreated(),
                savedUser.getModified(),
                savedUser.getLastLogin(),
                savedUser.getToken(),
                savedUser.isActive()
        );
    }

    @Transactional
    public void deleteUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }
        userRepository.deleteById(userId);
    }

    private void updatePhoneList(User existingUser, List<PhoneDto> phoneDtos) {
        // Limpiar la lista de teléfonos existente
        existingUser.getPhones().clear();

        // Mapear los nuevos PhoneDto a entidades Phone y asociarlos al usuario
        if (phoneDtos != null) {
            for (PhoneDto phoneDto : phoneDtos) {
                Phone phone = new Phone();
                phone.setNumber(phoneDto.getNumber());
                phone.setCitycode(phoneDto.getCitycode());
                phone.setCountrycode(phoneDto.getCountrycode());
                phone.setUser(existingUser);  // Asociar el teléfono con el usuario existente
                existingUser.getPhones().add(phone);
            }
        }
    }
}