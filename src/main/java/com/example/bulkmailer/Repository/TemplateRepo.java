package com.example.bulkmailer.Repository;

import com.example.bulkmailer.Entities.Template;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TemplateRepo extends JpaRepository<Template,Long> {
}
