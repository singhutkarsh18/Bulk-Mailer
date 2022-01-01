package com.example.bulkmailer.Controller;

import com.example.bulkmailer.Entities.DTOs.GroupRequest;
import com.example.bulkmailer.Services.GroupService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("/groups")
public class GroupsController {

    private GroupService groupService;
    @PostMapping("/add")
    public ResponseEntity<?> addGroups(@RequestBody GroupRequest groupRequest)
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(groupService.makeGroups(groupRequest));
    }
}
