package com.example.bulkmailer.Entities.DTOs;

import lombok.Getter;

import java.util.List;

@Getter
public class EmailRequest {
    private String groupId;
    private String subject;
    private String body;
    private List<String> attachment;
}
