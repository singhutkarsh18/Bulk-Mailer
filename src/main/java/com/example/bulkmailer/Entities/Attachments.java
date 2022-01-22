package com.example.bulkmailer.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;

@Entity@AllArgsConstructor@NoArgsConstructor
@Getter@Setter
public class Attachments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String attachmentName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "previous_mail_id",referencedColumnName = "id")
    private PreviousMail previousMail;
}
