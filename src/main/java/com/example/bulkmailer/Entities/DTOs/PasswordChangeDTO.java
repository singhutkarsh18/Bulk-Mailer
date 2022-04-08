package com.example.bulkmailer.Entities.DTOs;

import lombok.Getter;

@Getter
public class PasswordChangeDTO {
    private String oldPassword;
    private String newPassword;
}
