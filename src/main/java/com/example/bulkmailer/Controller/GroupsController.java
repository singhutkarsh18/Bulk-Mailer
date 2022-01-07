package com.example.bulkmailer.Controller;

import com.example.bulkmailer.Entities.AppUser;
import com.example.bulkmailer.Entities.DTOs.GroupRequest;
import com.example.bulkmailer.Repository.UserRepository;
import com.example.bulkmailer.Services.GroupService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;


@RestController@CrossOrigin("*")
@RequestMapping("/groups")
@AllArgsConstructor
public class GroupsController {

    private GroupService groupService;
    private UserRepository userRepository;

    @PostMapping("/add")
    public ResponseEntity<?> addGroups(@RequestBody GroupRequest groupRequest)
    {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(groupService.makeGroups(groupRequest));
        }
        catch (IllegalStateException e1)
        {
            return ResponseEntity.status(HttpStatus.ALREADY_REPORTED).body(e1.getLocalizedMessage());
        }
    }

    @GetMapping("/getAllGroups")
    public ResponseEntity<?> getAllGroups() {
        try {
            UserDetails userDetails=(UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String username=userDetails.getUsername();
            AppUser appUser = userRepository.findByUsername(username).get();
            if(appUser.getGroups()==null)
                throw new IllegalStateException("No group found");
            return ResponseEntity.status(HttpStatus.OK).body(appUser.getGroups());
        }
        catch (IllegalStateException e)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getLocalizedMessage());
        }
    }
    @GetMapping("/getEmails/{groupId}")
    public ResponseEntity<?> getEmails(@PathVariable String groupId)
    {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(groupService.getGroupEmails(groupId));
        }
        catch (UsernameNotFoundException e)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getLocalizedMessage());
        }
    }
    @DeleteMapping("/deleteGroup/{groupId}")
    public ResponseEntity<?> deleteGroups(@PathVariable String groupId)
    {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(groupService.deleteGroup(groupId));
        }
        catch (UsernameNotFoundException e)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getLocalizedMessage());
        }
    }
    @DeleteMapping("/deleteEmail/{emailId}")
    public ResponseEntity<?> deleteEmails(@PathVariable Long emailId)
    {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(groupService.deleteEmail(emailId));
        }
        catch (UsernameNotFoundException e)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getLocalizedMessage());
        }
    }
}
