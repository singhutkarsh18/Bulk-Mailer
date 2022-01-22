package com.example.bulkmailer.Entities.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class TemplateModel {

    private String groupId;
    private String subject;
    private Map<String, String> model;
    private List<String> attachment;
    private String templateName;
}
