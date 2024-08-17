package com.example.user_registration.config;

import com.example.user_registration.Util.PasswordValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ValidatorConfig {

    @Bean
    public PasswordValidator passwordValidator() {
        return new PasswordValidator();
    }
}
