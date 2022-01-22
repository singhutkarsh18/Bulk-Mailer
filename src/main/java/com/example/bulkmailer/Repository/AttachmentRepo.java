package com.example.bulkmailer.Repository;

import com.example.bulkmailer.Entities.Attachments;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepo extends JpaRepository<Attachments, Long> {
}
