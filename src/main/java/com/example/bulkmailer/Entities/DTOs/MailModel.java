package com.example.bulkmailer.Entities.DTOs;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.util.Map;

@Data
@ToString
@AllArgsConstructor
public class MailModel {

    private String to;
    private String subject;
    private Map<String, String> model;
}
