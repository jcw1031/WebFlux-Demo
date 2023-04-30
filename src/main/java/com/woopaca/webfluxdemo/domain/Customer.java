package com.woopaca.webfluxdemo.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class Customer {

    @Id
    private Long id;
    private final String firstName;
    private final String lastName;
}