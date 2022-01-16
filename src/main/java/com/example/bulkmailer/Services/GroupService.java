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
import java.util.*;

@AllArgsConstructor
@Service@Getter@Setter
public class GroupService {

    private GroupRepo groupRepo;
    private UserRepository userRepository;
    private EmailRepo emailRepo;

    public String makeGroups(GroupRequest groupRequest) {
        UserDetails userDetails=(UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username=userDetails.getUsername();
        String id= UUID.randomUUID().toString();
        Set<String> emails = new LinkedHashSet<>(groupRequest.getEmails());
        Groups group= new Groups(id,groupRequest.getName(),emails.size(),userRepository.findByUsername(username).get(),null);
        groupRepo.save(group);
        addEmails(id, emails);
        return "OK";
    }
    public void addEmails(String group_id, Set<String> emails)
    {
        if(emails.isEmpty())
            throw new EntityNotFoundException("No email found");
        List<Emails> e= new ArrayList<>();
        for (String email : emails) {
            e.add(new Emails(null, email, groupRepo.getById(group_id)));
        }
        emailRepo.saveAll(e);
    }
//    public List<String> removeDuplicates(List<String> emails)
//    {
//        Set<String> s = new LinkedHashSet<>();
//        s.addAll(emails);
//        emails.clear();
//        emails.addAll(s);
//        return emails;
//    }
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
        String groupId=emailRepo.findById(emailId).get().getGroups().getId();

        emailRepo.deleteById(emailId);
        Groups groups = groupRepo.findById(groupId).get();
        Integer count=groups.getCount();
        groups.setCount(count-1);
        groupRepo.save(groups);
        return "Email deleted";
    }

    public String updateEmails(String groupId, List<String> emails) {
        if(emails.isEmpty())
            throw new EntityNotFoundException("No email found");
        if(groupRepo.findById(groupId).isEmpty())
            throw new UsernameNotFoundException("Group not found");
        Set<String> emails1 = new LinkedHashSet<>(emails);
        Integer c=emails1.size();
        List<Emails> e= new ArrayList<>();
        for (String email : emails) {
            e.add(new Emails(null, email, groupRepo.getById(groupId)));
        }
        emailRepo.saveAll(e);
        Groups groups = groupRepo.findById(groupId).get();
        Integer count = groups.getCount();
        groups.setCount(count+c);
        groupRepo.save(groups);
        return "Emails added";
    }
}
