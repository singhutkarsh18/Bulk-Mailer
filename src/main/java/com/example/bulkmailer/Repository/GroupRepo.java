package com.example.bulkmailer.Repository;

import com.example.bulkmailer.Entities.Groups;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepo extends JpaRepository<Groups,Long> {
}
