package com.example.bulkmailer.Services;

import com.example.bulkmailer.Entities.DTOs.GroupRequest;
import com.example.bulkmailer.Entities.Emails;
import com.example.bulkmailer.Entities.Groups;
import com.example.bulkmailer.Repository.EmailRepo;
import com.example.bulkmailer.Repository.GroupRepo;
import com.example.bulkmailer.Repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
@Service@Getter@Setter@Transactional
public class GroupService {

    private GroupRepo groupRepo;
    private UserRepository userRepository;
    private EmailRepo emailRepo;

    public String makeGroups(GroupRequest groupRequest) {
        UserDetails userDetails=(UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username=userDetails.getUsername();
        String id= UUID.randomUUID().toString();
        Groups group= new Groups(id,groupRequest.getName(),userRepository.findByUsername(username).get(),null);
        groupRepo.save(group);
        addEmails(id, groupRequest.getEmails());
        return "OK";
    }
    public void addEmails(String group_id, List<String> emails)
    {
        if(emails.isEmpty())
            throw new EntityNotFoundException("No email found");
        for (String email : emails) {
            emailRepo.save(new Emails(null, email, groupRepo.getById(group_id)));
        }

    }
    public Set<Emails> getGroupEmails(String groupId) {
        if(groupRepo.findById(groupId).isEmpty())
            throw new UsernameNotFoundException("No email found");
        return groupRepo.findById(groupId).get().getEmails();
    }

    public String deleteGroup(String groupId) {
        if(groupRepo.findById(groupId).isEmpty())
            throw new UsernameNotFoundException("Group not found");
        groupRepo.deleteById(groupId);
        return "Group deleted";
    }

    public String deleteEmail(Long emailId) {
        if(emailRepo.findById(emailId).isEmpty())
            throw new UsernameNotFoundException("Email not found");
        emailRepo.deleteById(emailId);
        return "Email deleted";
    }
}
