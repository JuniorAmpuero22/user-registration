package com.example.user_registration.Util;

import org.springframework.beans.factory.annotation.Value;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    @Value("${password.regex}")
    private String passwordRegex;

    @Override
    public void initialize(ValidPassword constraintAnnotation) {
        // Initialization if needed
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        return password != null && password.matches(passwordRegex);
    }
}
