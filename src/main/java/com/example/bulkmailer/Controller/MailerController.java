package com.example.bulkmailer.Controller;

import com.example.bulkmailer.Services.BulkMailService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
@Slf4j@AllArgsConstructor
public class MailerController {
    @Autowired
    private BulkMailService bulkMailService;


    @PostMapping("/send")
    public String testMail(@RequestBody List<String> recipients)
    {
        recipients.stream().forEach(p->log.info(p));
        bulkMailService.sendBulk(recipients);
        return "OK";

    }
}
