package com.example.bulkmailer.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity@AllArgsConstructor
@NoArgsConstructor@Getter@Setter
public class Template {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String name;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name="user_id",referencedColumnName = "id")
    private AppUser appUser;
}
