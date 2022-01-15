package com.example.bulkmailer.Entities.DTOs;

import lombok.Data;

import java.util.List;

@Data
public class UpdateGroupReq {
    String groupId;
    List<String> emails;
}
