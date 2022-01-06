package com.example.bulkmailer.Entities.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class TemplateModel {

    private long groupId;
    private String subject;
    private Map<String, String> model;
    private String attachment;
    private String templateName;
}
