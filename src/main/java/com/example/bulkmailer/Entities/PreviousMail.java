package com.example.bulkmailer.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity@Getter@Setter
@NoArgsConstructor@AllArgsConstructor
public class PreviousMail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String subject;
    private String groupName;
    private String date;
    private String time;

    @OneToMany(fetch = FetchType.EAGER,mappedBy = "previousMail",cascade = CascadeType.ALL)
    private Set<Attachments> attachment=new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",referencedColumnName = "id")
    private AppUser appUser;
}
