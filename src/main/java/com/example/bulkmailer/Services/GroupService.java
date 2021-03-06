package com.example.bulkmailer.Services;

import com.example.bulkmailer.Entities.AppUser;
import com.example.bulkmailer.Entities.DTOs.GroupRequest;
import com.example.bulkmailer.Entities.DTOs.GroupWithNameReq;
import com.example.bulkmailer.Entities.DTOs.NameEmail;
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
import java.util.*;

@AllArgsConstructor
@Service@Getter@Setter
public class GroupService {

    private final String emailRegex="^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[\\a-zA-Z]{2,6}";
    private GroupRepo groupRepo;
    private UserRepository userRepository;
    private EmailRepo emailRepo;
    private RegisterService registerService;

    public String makeGroups(GroupRequest groupRequest,Boolean hasName) {
        UserDetails userDetails=(UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username=userDetails.getUsername();
        //TODO: User should only get his groups name not duplicate
        AppUser appUser = userRepository.findByUsername(username).get();
        Set<Groups> groups = appUser.getGroups();
        groups.forEach((n)->{
            if(n.getName().equals(groupRequest.getName()))
                throw new UnsupportedOperationException("Name already taken");
        });
        String id= UUID.randomUUID().toString();
        Set<String> emails = new LinkedHashSet<>(groupRequest.getEmails());
        Groups group= new Groups(id,groupRequest.getName(),emails.size(),hasName,userRepository.findByUsername(username).get(),null);
        groupRepo.save(group);
        addEmails(id, emails);
        return "OK";
    }
    public void addEmails(String group_id, Set<String> emails)
    {
        //TODO: check emails are valid!!
        if(emails.isEmpty())
            throw new EntityNotFoundException("No email found");
        List<Emails> e= new ArrayList<>();
        for (String email : emails) {
            e.add(new Emails(null,null, email, groupRepo.getById(group_id)));
        }
        emailRepo.saveAll(e);
    }
    public Object getGroupEmails(String groupId) {
        if(groupRepo.findById(groupId).isEmpty())
            throw new UsernameNotFoundException("No email found");
        Groups groups =groupRepo.findById(groupId).get();
        List<Emails> emailsList = new ArrayList<>(groups.getEmails());
        emailsList.sort((Emails a,Emails b)->{
            return a.getId()<b.getId()?-1:1;
        });
        Map<String,Object> res = new HashMap<>();
        res.put("hasName",groups.getHasName());
        res.put("count",groups.getCount());
        res.put("emails",emailsList);
        return res;
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

    public List<Emails> updateEmails(String groupId, List<String> emails) {
        if(emails.isEmpty())
            throw new EntityNotFoundException("No email found");
        if(groupRepo.findById(groupId).isEmpty())
            throw new UsernameNotFoundException("Group not found");
        Set<String> emails1 = new LinkedHashSet<>(emails);
        Integer c=emails1.size();
        List<Emails> e= new ArrayList<>();
        for (String email : emails) {
            e.add(new Emails(null,null, email, groupRepo.getById(groupId)));
        }
        List<Emails> updatedEmailsList = emailRepo.saveAll(e);
        Groups groups = groupRepo.findById(groupId).get();
        Integer count = groups.getCount();
        groups.setCount(count+c);
        groupRepo.save(groups);
        return updatedEmailsList;
    }

    public String makeGroupsWtihName(GroupWithNameReq groupRequest,Boolean hasName) {
        UserDetails userDetails=(UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username=userDetails.getUsername();
        String id= UUID.randomUUID().toString();
        AppUser appUser = userRepository.findByUsername(username).get();
        Set<Groups> groups = appUser.getGroups();
        groups.forEach((n)->{
            if(n.getName().equals(groupRequest.getName()))
                throw new UnsupportedOperationException("Name already taken");
        });
        Set<NameEmail> nameEmails = new LinkedHashSet<>(groupRequest.getNameEmail());
        Groups group= new Groups(id,groupRequest.getName(),nameEmails.size(),hasName,userRepository.findByUsername(username).get(),null);
        groupRepo.save(group);
        addEmailsWithName(id, nameEmails);
        return "OK";
    }
    public void addEmailsWithName(String group_id, Set<NameEmail> nameEmail)
    {
        if(nameEmail.isEmpty())
            throw new EntityNotFoundException("No email found");
        List<Emails> e= new ArrayList<>();
        for (NameEmail nameEmail1 : nameEmail) {
            e.add(new Emails(null, nameEmail1.getName(), nameEmail1.getEmail(), groupRepo.getById(group_id)));
        }
        emailRepo.saveAll(e);
    }
    public List<Emails> updateEmailsWithName(String groupId, Set<NameEmail> nameEmail) {
        if(nameEmail.isEmpty())
            throw new EntityNotFoundException("No email found");
        if(groupRepo.findById(groupId).isEmpty())
            throw new UsernameNotFoundException("Group not found");
        Set<NameEmail> nameEmail1 = new LinkedHashSet<>(nameEmail);
        Integer c=nameEmail.size();
        List<Emails> e= new ArrayList<>();
        for (NameEmail nameEmail2 : nameEmail) {
            e.add(new Emails(null, nameEmail2.getName(), nameEmail2.getEmail(), groupRepo.getById(groupId)));
        }
        List<Emails> updatedEmailsList = emailRepo.saveAll(e);
        Groups groups = groupRepo.findById(groupId).get();
        Integer count = groups.getCount();
        groups.setCount(count+c);
        groupRepo.save(groups);
        return updatedEmailsList;
    }
}
