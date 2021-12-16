package com.example.bulkmailer.Entities;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter@Setter@EqualsAndHashCode
public class RegistrationRequest {
    private String name;
    private String username;
    private String password;

}
