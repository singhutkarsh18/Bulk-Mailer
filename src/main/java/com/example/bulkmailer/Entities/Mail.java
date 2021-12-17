package com.example.bulkmailer.Entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter@Setter@AllArgsConstructor
public class Mail {
    private String recipient;
    private String subject;
    private String message;


}