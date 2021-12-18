package com.example.bulkmailer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Properties;

@SpringBootApplication
public class BulkMailerApplication {

    public static void main(String[] args) {
        Properties props= new Properties();
        props.put("mail.debug", "true");
        SpringApplication.run(BulkMailerApplication.class, args);
    }

}
