package com.example.bulkmailer.Controller;

import com.example.bulkmailer.Entities.AppUser;
import com.example.bulkmailer.Entities.DTOs.GroupRequest;
import com.example.bulkmailer.Entities.DTOs.GroupWithNameReq;
import com.example.bulkmailer.Entities.DTOs.UpdateGroupReq;
import com.example.bulkmailer.Entities.DTOs.UpdateNameEmail;
import com.example.bulkmailer.Entities.Groups;
import com.example.bulkmailer.Repository.UserRepository;
import com.example.bulkmailer.Services.GroupService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;


@RestController@CrossOrigin("*")
@RequestMapping("/groups")
@AllArgsConstructor
public class GroupsController {

    private GroupService groupService;
    private UserRepository userRepository;

    @PostMapping("/add/{hasName}")
    public ResponseEntity<?> addGroups(@RequestBody GroupRequest groupRequest,@PathVariable Boolean hasName)
    {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(groupService.makeGroups(groupRequest,hasName));
        }
        catch (EntityNotFoundException e1)
        {
            return ResponseEntity.status(HttpStatus.ALREADY_REPORTED).body(e1.getLocalizedMessage());
        }
        catch (UnsupportedOperationException e2)
        {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(e2.getLocalizedMessage());
        }
    }
    @PostMapping("/addWithName/{hasName}")
    public ResponseEntity<?> addWithName(@RequestBody GroupWithNameReq groupRequest,@PathVariable Boolean hasName)
    {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(groupService.makeGroupsWtihName(groupRequest,hasName));
        }
        catch (IllegalStateException e1)
        {
            return ResponseEntity.status(HttpStatus.ALREADY_REPORTED).body(e1.getLocalizedMessage());
        }
        catch (UnsupportedOperationException e2)
        {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(e2.getLocalizedMessage());
        }
    }
    @GetMapping("/getAllGroups")
    public ResponseEntity<?> getAllGroups() {
        try {
            UserDetails userDetails=(UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String username=userDetails.getUsername();
            AppUser appUser = userRepository.findByUsername(username).get();
            if(appUser.getGroups()==null)//see once more
                throw new IllegalStateException("No group found");
            List<Groups> groupsList= new ArrayList<>(appUser.getGroups());
            groupsList.sort((Groups a,Groups b)->{
                return a.getName().compareTo(b.getName());
            });
            return ResponseEntity.status(HttpStatus.OK).body(groupsList);
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
    @PutMapping("/updateGroup")
    public ResponseEntity<?> updateGroup(@RequestBody UpdateGroupReq updateGroupReq)
    {
        try{
            return ResponseEntity.status(HttpStatus.OK).body(groupService.updateEmails(updateGroupReq.getGroupId(),updateGroupReq.getEmails()));
        }
        catch (UsernameNotFoundException e1)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e1.getLocalizedMessage());
        }
        catch (EntityNotFoundException e2)
        {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(e2.getLocalizedMessage());
        }
    }
    @PutMapping("/updateGroupWithName")
    public ResponseEntity<?> updateGroupWithName(@RequestBody UpdateNameEmail updateNameEmail)
    {
        try{
            return ResponseEntity.status(HttpStatus.OK).body(groupService.updateEmailsWithName(updateNameEmail.getGroupId(),updateNameEmail.getNameEmails()));
        }
        catch (UsernameNotFoundException e1)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e1.getLocalizedMessage());
        }
        catch (EntityNotFoundException e2)
        {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(e2.getLocalizedMessage());
        }
    }

}
