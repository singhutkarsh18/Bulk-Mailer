package com.example.bulkmailer.Controller;

import com.example.bulkmailer.Entities.DTOs.EmailRequest;
import com.example.bulkmailer.Entities.DTOs.MailModel;
import com.example.bulkmailer.Entities.DTOs.Recipients;
import com.example.bulkmailer.Entities.DTOs.TemplateModel;
import com.example.bulkmailer.Repository.GroupRepo;
import com.example.bulkmailer.Services.BulkMailService;
import freemarker.template.TemplateException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.NoSuchElementException;

@Slf4j@AllArgsConstructor
@RestController@CrossOrigin("*")
@RequestMapping("/mail")
public class MailerController {
    @Autowired
    private BulkMailService bulkMailService;

    private GroupRepo groupRepo;


    @PostMapping("/send")
    public String testMail(@RequestBody Recipients recipients)
    {
        recipients.getEmails().stream().forEach(p->log.info(p));
        bulkMailService.sendBulk(recipients);
        return "OK";

    }
    @PostMapping("/sendMail")
    public ResponseEntity<?> sendMail(@RequestBody EmailRequest emailRequest)
    {
        try{
            return ResponseEntity.status(HttpStatus.OK).body(bulkMailService.sendBulkMail(emailRequest));
        }
        catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
        catch (MessagingException | UnsupportedEncodingException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(e.getMessage());
        }

    }
    @PostMapping("/sendTemplate")
    public ResponseEntity<?> sendTemplate(@RequestBody MailModel mailModel) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(bulkMailService.sendEmail(mailModel));
        } catch (MessagingException | TemplateException | IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(e.getMessage());
        }
    }
    @PostMapping("/sendBulkTemplates")
    public ResponseEntity<?> sendBulkTemplates(@RequestBody TemplateModel templateModel)
    {
        try{
            bulkMailService.sendEmailTemplates(templateModel);
            return ResponseEntity.status(HttpStatus.OK).body("Mail sent");
        }
        catch (NoSuchElementException e)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
        catch (MessagingException | TemplateException | IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(e.getMessage());
        }

    }
    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file,
                                         @RequestParam("fileName") String name) {
        try {
            String imageDirectory =new ClassPathResource("static/uploads").getFile().getAbsolutePath();
            makeDirectoryIfNotExist(imageDirectory);
            String[] fileFrags = file.getOriginalFilename().split("\\.");
            String extension = fileFrags[fileFrags.length-1];
            Path fileNamePath = Paths.get(imageDirectory,
                    name.concat(".").concat(extension));
            System.out.println(fileNamePath);
            Files.write(fileNamePath, file.getBytes());
            return new ResponseEntity<>(ServletUriComponentsBuilder.fromCurrentContextPath().path("/static").path("/uploads/").path(name.concat("."+extension)).toUriString(), HttpStatus.CREATED);
        } catch (IOException ex) {
            ex.printStackTrace();
            return new ResponseEntity<>("File is not uploaded", HttpStatus.BAD_REQUEST);
        }
    }
    @PostMapping("/uploadTemplate")
    public ResponseEntity<?> uploadTemplate(@RequestParam("file") MultipartFile file,
                                         @RequestParam("fileName") String name) {
        try {
            String imageDirectory =new ClassPathResource("static/templates").getFile().getAbsolutePath();
            makeDirectoryIfNotExist(imageDirectory);
            String[] fileFrags = file.getOriginalFilename().split("\\.");
            String extension = fileFrags[fileFrags.length-1];
            Path fileNamePath = Paths.get(imageDirectory,
                    name.concat(".").concat(extension));
            System.out.println(fileNamePath);
            Files.write(fileNamePath, file.getBytes());
            return new ResponseEntity<>("Template uploaded", HttpStatus.CREATED);
        } catch (Exception ex) {
            return new ResponseEntity<>("Template is not uploaded", HttpStatus.BAD_REQUEST);
        }
    }
    private void makeDirectoryIfNotExist(String imageDirectory) {
        File directory = new File(imageDirectory);
        if (!directory.exists()) {
            directory.mkdir();
        }
    }

}
