package com.example.user_registration.dto;

import com.example.user_registration.Util.ValidPassword;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import java.util.List;


@Data
public class UserDto {
    @NotEmpty(message = "El nombre no puede estar vacío")
    private String name;

    @NotEmpty(message = "El correo no puede estar vacío")
    @Email(message = "El correo no es válido")
    @Pattern(regexp = "^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$", message = "El formato del correo no es válido")
    private String email;

    @NotEmpty(message = "La contraseña no puede estar vacía")
    @ValidPassword
    private String password;

    private List<PhoneDto> phones;
}
