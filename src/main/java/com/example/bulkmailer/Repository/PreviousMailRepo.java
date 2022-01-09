package com.example.bulkmailer.Repository;

import com.example.bulkmailer.Entities.PreviousMail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PreviousMailRepo extends JpaRepository<PreviousMail,Long> {
}
