package com.example.bulkmailer.Entities.DTOs;

import lombok.Getter;

import java.util.List;

@Getter
public class Recipients {
    private List<String> emails;
    private String subject;
    private String body;
}
