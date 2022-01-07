package com.example.bulkmailer.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Getter@Setter
@AllArgsConstructor@NoArgsConstructor
@Entity
public class Groups {
    @Id
    private String id;
    private String name;

    @ManyToOne@JsonIgnore
    @JoinColumn(name="user_id",referencedColumnName = "id")
    private AppUser appUser;

    @OneToMany(fetch = FetchType.EAGER,mappedBy = "groups",cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<Emails> emails=new HashSet<>();
    
}
