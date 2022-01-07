package com.example.bulkmailer.Entities.DTOs;

import lombok.Getter;

@Getter
public class EmailRequest {
    private String groupId;
    private String subject;
    private String body;
    private String attachment;
}
