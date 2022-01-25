package com.example.bulkmailer.Entities.DTOs;

import lombok.Getter;

import java.util.Set;
@Getter
public class UpdateNameEmail {
    private String groupId;
    private Set<NameEmail> nameEmails;
}
