package com.example.user_registration.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PhoneDto {

    private String number;
    private String cityCode;
    private String countryCode;
}
