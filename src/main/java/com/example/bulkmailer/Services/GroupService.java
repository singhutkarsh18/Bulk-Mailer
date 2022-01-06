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

import javax.transaction.Transactional;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@Service@Getter@Setter@Transactional
public class GroupService {

    private GroupRepo groupRepo;
    private UserRepository userRepository;
    private EmailRepo emailRepo;

    public String makeGroups(GroupRequest groupRequest) {
        UserDetails userDetails=(UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username=userDetails.getUsername();
        if(!(groupRepo.findByName(groupRequest.getName()) == null))
            throw new IllegalStateException("Name already present");
        Groups group= new Groups(null,groupRequest.getName(),userRepository.findByUsername(username).get(),null);
        groupRepo.save(group);
        addEmails(groupRepo.findByName(group.getName()).getId(), groupRequest.getEmails());
        return "OK";
    }
    public void addEmails(Long group_id, List<String> emails)
    {
        Iterator itr=emails.iterator();
        while(itr.hasNext())
        {
            emailRepo.save(new Emails(null,(String) itr.next(),groupRepo.getById(group_id)));
        }
    }
    public Set<Emails> getGroupEmails(Long groupId) {
        if(groupRepo.findById(groupId).isEmpty())
            throw new UsernameNotFoundException("No email found");
        return groupRepo.findById(groupId).get().getEmails();
    }

    public String deleteGroup(Long groupId) {
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
