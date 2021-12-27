package com.example.bulkmailer.Services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;

@Service @Slf4j @AllArgsConstructor
public class BulkMailService {

    private JavaMailSender mailSender;

    private MailService mailService;
    @Async("threadPoolTaskExecutor")
    public void sendBulk(List<String> recipeint) {

        try {

//            Iterator itr = mail.getRecipient().iterator();
            Iterator itr = recipeint.iterator();
            Long start = System.currentTimeMillis();
            log.info("StartTime-{}", start);
            LocalDateTime l=LocalDateTime.now();

            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true);
            helper.setFrom(String.valueOf(new InternetAddress("loadingerror144@gmail.com")), "Bulk mailer");
            helper.setText("Async message"+l.getSecond(), true);
            helper.addAttachment("image.jpg", new File("C:\\Users\\utkar\\Desktop\\image.jpg"));
            while(itr.hasNext())
            {
                String recipient = (String) itr.next();
                msg.addRecipients(Message.RecipientType.BCC,recipient);
            }
            mailService.sendMail(msg);
            Long end = System.currentTimeMillis();
            log.info("EndTime-{}", end);
            log.info("Execution time - {}", end - start);
        }
        catch (MessagingException e1)
        {
            log.error(e1.toString());
        }
        catch ( UnsupportedEncodingException e2)
        {
            log.error(e2.toString());
        }
//        return (end-start);
    }

}
