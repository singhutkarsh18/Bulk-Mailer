package com.example.bulkmailer.Entities.DTOs;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter@Setter
public class GroupRequest {
    private String name;
    private List<String> emails;
}
