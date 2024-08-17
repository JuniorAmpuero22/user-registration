package com.example.user_registration.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class Phone {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String number;
    private String citycode;
    private String countrycode;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}