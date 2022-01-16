package com.example.bulkmailer.Services;

import com.example.bulkmailer.Entities.AppUser;
import com.example.bulkmailer.Entities.DTOs.EmailRequest;
import com.example.bulkmailer.Entities.DTOs.Recipients;
import com.example.bulkmailer.Entities.DTOs.TemplateModel;
import com.example.bulkmailer.Entities.Emails;
import com.example.bulkmailer.Entities.Groups;
import com.example.bulkmailer.Entities.DTOs.MailModel;
import com.example.bulkmailer.Entities.PreviousMail;
import com.example.bulkmailer.Repository.GroupRepo;
import com.example.bulkmailer.Repository.PreviousMailRepo;
import com.example.bulkmailer.Repository.UserRepository;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service @Slf4j @AllArgsConstructor@Transactional
public class BulkMailService {

    private JavaMailSender mailSender;

    @Autowired
    @Qualifier("emailConfigBean")
    private Configuration emailConfig;

    private MailService mailService;

    private GroupRepo groupRepo;

    private PreviousMailRepo previousMailRepo;

    private UserRepository userRepository;

    @Async("threadPoolTaskExecutor")
    public void sendBulk(Recipients recipients){

        try {

            Iterator itr = removeDuplicates(recipients.getEmails()).iterator();
            Long start = System.currentTimeMillis();
            log.info("StartTime-{}", start);

            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true);
            helper.setFrom(String.valueOf(new InternetAddress("loadingerror144@gmail.com")), "Bulk mailer");
            helper.setText(recipients.getBody(), true);
            helper.setSubject(recipients.getSubject());
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
    public String sendBulkMail(EmailRequest emailRequest) throws MessagingException,UnsupportedEncodingException {

            if(groupRepo.findById(emailRequest.getGroupId()).isEmpty())
                throw new NoSuchElementException("Group not found");
            Groups groups = groupRepo.findById(emailRequest.getGroupId()).get();
            Iterator itr = groups.getEmails().iterator();
            Long start = System.currentTimeMillis();
            log.info("StartTime-{}", start);

            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true);
            helper.setFrom(String.valueOf(new InternetAddress("loadingerror144@gmail.com")), "Bulk mailer");
            helper.setText(emailRequest.getBody(), true);
            helper.setSubject(emailRequest.getSubject());
            if(emailRequest.getAttachment()!=null)
                helper.addAttachment(emailRequest.getAttachment(), new File("./src/main/resources/uploads/"+emailRequest.getAttachment()));
            while(itr.hasNext())
            {
                Emails emails = (Emails) itr.next();
                String email=emails.getEmail();
                msg.addRecipients(Message.RecipientType.BCC,email);
            }
            mailService.sendMail(msg);
            Long end = System.currentTimeMillis();
            log.info("EndTime-{}", end);
            log.info("Execution time - {}", end - start);
            return "Email sent";
    }
    public List<String> removeDuplicates(List<String> emails)
    {
        Set<String> s = new LinkedHashSet<>();
        s.addAll(emails);
        emails.clear();
        emails.addAll(s);
        return emails;
    }


    public String sendEmail(MailModel mailModel) throws MessagingException, IOException, TemplateException {

        long start=System.currentTimeMillis();
        mailModel.setModel(mailModel.getModel());


        log.info("Sending Email to: " + mailModel.getTo());


        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
        Template template = emailConfig.getTemplate("email.ftl");
        String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, mailModel.getModel());

        mimeMessageHelper.setTo(mailModel.getTo());
        mimeMessageHelper.setText(html, true);
        mimeMessageHelper.setSubject(mailModel.getSubject());
        mimeMessageHelper.setFrom(String.valueOf(new InternetAddress("loadingerror144@gmail.com")), "Bulk mailer");
        FileSystemResource res = new FileSystemResource(new File("./src/main/resources/uploads/silogo.png"));
        mimeMessageHelper.addInline("image", res);


        mailSender.send(message);
        long end=System.currentTimeMillis();
        log.info("Time -{}",end-start);
        return "mail sent";
    }


    public String sendEmailTemplates(TemplateModel templateModel) throws MessagingException, IOException, TemplateException {
        long start=System.currentTimeMillis();

        if(groupRepo.findById(templateModel.getGroupId()).isEmpty())
            throw new NoSuchElementException("Group not found");
        Groups groups = groupRepo.findById(templateModel.getGroupId()).get();
        Iterator itr = groups.getEmails().iterator();

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
        Template template = emailConfig.getTemplate(templateModel.getTemplateName());
        String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, templateModel.getModel());
        while(itr.hasNext())
        {
            Emails emails = (Emails) itr.next();
            String email=emails.getEmail();
            log.info("Sending Email to: "+email);
            message.addRecipients(Message.RecipientType.BCC,email);
        }
        mimeMessageHelper.setText(html, true);
        mimeMessageHelper.setSubject(templateModel.getSubject());
        mimeMessageHelper.setFrom(String.valueOf(new InternetAddress("loadingerror144@gmail.com")), "SI mailer");
        mimeMessageHelper.addInline("image", new FileSystemResource(new File("./src/main/resources/uploads/"+templateModel.getModel().get("logo"))));
        if(templateModel.getAttachment()!=null)
            mimeMessageHelper.addAttachment(templateModel.getAttachment(), new File("./src/main/resources/uploads/"+templateModel.getAttachment()));
        mailSender.send(message);
        addToHistory(message.getSubject(), getTextFromMessage(message),groups.getName(),templateModel.getAttachment());
        long end=System.currentTimeMillis();
        log.info("Time -{}",end-start);
        return "mail sent";
    }
    public void addToHistory(String subject, String body, String groupName, String attachmentName)
    {
        UserDetails userDetails=(UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username=userDetails.getUsername();
        previousMailRepo.save(new PreviousMail(null,subject,body,groupName,attachmentName,userRepository.findByUsername(username).get()));

    }
    private String getTextFromMessage(Message message) throws MessagingException, IOException {
        String result = "";
        if (message.isMimeType("text/plain")) {
            result = message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            result = getTextFromMimeMultipart(mimeMultipart);
        }
        return result;
    }
    private String getTextFromMimeMultipart(
            MimeMultipart mimeMultipart)  throws MessagingException, IOException{
        String result = "";
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result = result + "\n" + bodyPart.getContent();
                break; // without break same text appears twice in my tests
            } else if (bodyPart.isMimeType("text/html")) {
                String html = (String) bodyPart.getContent();
                result = result + "\n" + org.jsoup.Jsoup.parse(html).text();
            } else if (bodyPart.getContent() instanceof MimeMultipart){
                result = result + getTextFromMimeMultipart((MimeMultipart)bodyPart.getContent());
            }
        }
        return result;
    }
}

