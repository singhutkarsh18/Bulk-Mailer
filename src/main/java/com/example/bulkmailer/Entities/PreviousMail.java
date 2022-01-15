package com.example.bulkmailer.Entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity@Getter@Setter
@NoArgsConstructor@AllArgsConstructor
public class PreviousMail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String body;
    private String groupName;
    private String attachmentName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",referencedColumnName = "id")
    private AppUser appUser;
}
