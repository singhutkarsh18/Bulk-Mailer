package com.example.bulkmailer.Services;

import com.example.bulkmailer.Entities.DTOs.GroupRequest;
import com.example.bulkmailer.Entities.Groups;
import com.example.bulkmailer.Repository.GroupRepo;
import com.example.bulkmailer.Repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service@AllArgsConstructor
public class GroupService {

    private GroupRepo groupRepo;
    private UserRepository userRepository;

    public String makeGroups(GroupRequest groupRequest) {
        UserDetails userDetails=(UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username=userDetails.getUsername();
        groupRepo.save(new Groups(null,groupRequest.getName(),userRepository.findByUsername(username).get()));
        return "OK";
    }
}
