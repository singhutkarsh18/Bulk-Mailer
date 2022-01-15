package com.example.bulkmailer.Repository;

import com.example.bulkmailer.Entities.Emails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailRepo extends JpaRepository<Emails,Long> {
}
