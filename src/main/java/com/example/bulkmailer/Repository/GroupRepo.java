package com.example.bulkmailer.Repository;

import com.example.bulkmailer.Entities.Groups;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

@Repository
public interface GroupRepo extends JpaRepository<Groups,String> {
    Groups findByName(String name);
}
