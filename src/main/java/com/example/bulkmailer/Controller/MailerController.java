package com.example.bulkmailer.Controller;

import com.example.bulkmailer.Entities.DTOs.EmailRequest;
import com.example.bulkmailer.Entities.DTOs.TemplateModel;
import com.example.bulkmailer.Repository.GroupRepo;
import com.example.bulkmailer.Services.BulkMailService;
import freemarker.template.TemplateException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.mail.MessagingException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j@AllArgsConstructor
@RestController@CrossOrigin("*")
@RequestMapping("/mail")
public class MailerController {
    @Autowired
    private BulkMailService bulkMailService;

    private GroupRepo groupRepo;
    private final String directory =System.getProperty("user.dir")+"/target/classes/uploads";

    @PostMapping("/sendMail")
    public ResponseEntity<?> sendMail(@RequestBody EmailRequest emailRequest)
    {
        try{
            return ResponseEntity.status(HttpStatus.OK).body(bulkMailService.sendBulkMail(emailRequest));
        }
        catch (NoSuchElementException e1) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e1.getMessage());
        }
        catch (IllegalStateException e2)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e2.getMessage());
        }
        catch (MessagingException | IOException e3) {
            e3.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(e3.getMessage());
        }

    }
    @PostMapping("/sendBulkTemplates")
    public ResponseEntity<?> sendBulkTemplates(@RequestBody TemplateModel templateModel)
    {
        try{
            bulkMailService.sendEmailTemplates(templateModel);
            return ResponseEntity.status(HttpStatus.OK).body("Mail sent");
        }
        catch (NoSuchElementException e1)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e1.getMessage());
        }
        catch (IllegalStateException e2)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e2.getMessage());
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
            bulkMailService.saveFile(directory,file,name);
            String[] fileFrags = file.getOriginalFilename().split("\\.");
            Map<String,Object> res=new HashMap<>();
            res.put("file",ServletUriComponentsBuilder.fromCurrentContextPath().path("/static").path("/uploads/").path(name.concat("."+fileFrags[fileFrags.length-1])).toUriString());
            res.put("fileName",(name.concat("."+fileFrags[fileFrags.length-1])));
            return new ResponseEntity<>(res, HttpStatus.CREATED);
        } catch (IOException ex) {
            ex.printStackTrace();
            return new ResponseEntity<>("File is not uploaded", HttpStatus.BAD_REQUEST);
        }
    }

}
