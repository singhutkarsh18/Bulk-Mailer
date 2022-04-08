package com.example.bulkmailer.Entities.DTOs;


import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class TemplateModel {

    private String from;
    private String groupId;
    private String subject;
    private Map<String, String> model;
    private List<String> attachment;
    private String templateName;


}
