package com.example.bulkmailer.Services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Arrays;

@Service @Slf4j @AllArgsConstructor
public class MailService {

    private JavaMailSender javaMailSender;

    public void sendMail(MimeMessage message)
    {
        log.info(Thread.currentThread().getName());
        javaMailSender.send(message);

        try {
            Arrays.stream(message.getRecipients(Message.RecipientType.BCC)).sequential().forEach(p->log.info("Message sent -{}",p.toString() ));
        }
        catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
