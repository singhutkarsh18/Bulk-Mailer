package com.example.bulkmailer.Entities.DTOs;

import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class NameReq {
    private String from;
    private String subject;
    private List<String> attachment;
    private String logo;
    private Long templateId;
    private String groupId;
    private Map<String, String> model;
}
