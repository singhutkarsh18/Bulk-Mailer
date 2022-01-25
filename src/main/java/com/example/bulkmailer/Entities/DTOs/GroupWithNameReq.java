package com.example.bulkmailer.Entities.DTOs;

import lombok.Getter;

import java.util.List;

@Getter
public class GroupWithNameReq {
    private String name;
    private List<NameEmail> nameEmail;
}
