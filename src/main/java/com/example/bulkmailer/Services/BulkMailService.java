package com.example.bulkmailer.Services;

import com.example.bulkmailer.Entities.Attachments;
import com.example.bulkmailer.Entities.DTOs.EmailRequest;
import com.example.bulkmailer.Entities.DTOs.NameReq;
import com.example.bulkmailer.Entities.DTOs.TemplateModel;
import com.example.bulkmailer.Entities.Emails;
import com.example.bulkmailer.Entities.Groups;
import com.example.bulkmailer.Entities.PreviousMail;
import com.example.bulkmailer.Repository.AttachmentRepo;
import com.example.bulkmailer.Repository.GroupRepo;
import com.example.bulkmailer.Repository.PreviousMailRepo;
import com.example.bulkmailer.Repository.TemplateRepo;
import com.example.bulkmailer.Repository.UserRepository;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.itextpdf.tool.xml.exceptions.RuntimeWorkerException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

@Service @Slf4j @AllArgsConstructor@Transactional
public class BulkMailService {

    private JavaMailSender mailSender;


    private Configuration emailConfig;

    private MailService mailService;

    private GroupRepo groupRepo;

    private PreviousMailRepo previousMailRepo;

    private UserRepository userRepository;

    private AttachmentRepo attachmentRepo;

    private TemplateRepo templateRepo;

    private final String directory =System.getProperty("user.dir")+"/target/classes/uploads";

    public String sendBulkMail(EmailRequest emailRequest) throws MessagingException,UnsupportedEncodingException {
        try {
            if (groupRepo.findById(emailRequest.getGroupId()).isEmpty())
                throw new NoSuchElementException("Group not found");
            Groups groups = groupRepo.findById(emailRequest.getGroupId()).get();
            Iterator itr = groups.getEmails().iterator();
            Long start = System.currentTimeMillis();
            log.info("StartTime-{}", start);

            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true);
            helper.setFrom(String.valueOf(new InternetAddress("loadingerror144@gmail.com")), emailRequest.getFrom());
            helper.setText(emailRequest.getBody(), true);
            helper.setSubject(emailRequest.getSubject());
            if (emailRequest.getAttachment() != null) {
                for (String fileName : emailRequest.getAttachment()) {
                    log.info("Attachment added : {}", fileName);
                    helper.addAttachment(fileName, new File(directory + "/" + fileName));
                }
            }
            while (itr.hasNext()) {
                Emails emails = (Emails) itr.next();
                String email = emails.getEmail();
                msg.addRecipients(Message.RecipientType.BCC, email);
            }
            mailService.sendMail(msg);
            addToHistory(msg.getSubject(), groups.getName(), new HashSet<>(emailRequest.getAttachment()));
            Long end = System.currentTimeMillis();
            log.info("EndTime-{}", end);
            log.info("Execution time - {}", end - start);
            return "Email sent";
        }
        catch (MessagingException | IOException |RuntimeException e)
        {
            return "Mail not sent";
        }
    }

    public String sendEmailTemplates(TemplateModel templateModel) throws MessagingException, IOException, TemplateException {
        long start=System.currentTimeMillis();

        if(groupRepo.findById(templateModel.getGroupId()).isEmpty())
            throw new NoSuchElementException("Group not found");
        Groups groups = groupRepo.findById(templateModel.getGroupId()).get();
        Iterator itr = groups.getEmails().iterator();

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
        emailConfig.setDirectoryForTemplateLoading(new File(directory));
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
        mimeMessageHelper.setFrom(String.valueOf(new InternetAddress("loadingerror144@gmail.com")), templateModel.getFrom());
        mimeMessageHelper.addInline("image", new FileSystemResource(new File(directory+"/"+templateModel.getModel().get("logo"))));
        if(templateModel.getAttachment()!=null)
        {
            for(String emailName:templateModel.getAttachment()){
                mimeMessageHelper.addAttachment(emailName,new File(directory+templateModel.getAttachment()));
            }
        }
        mailSender.send(message);
        addToHistory(message.getSubject(),groups.getName(),new HashSet<>(templateModel.getAttachment()));
        long end=System.currentTimeMillis();
        log.info("Time -{}",end-start);
        return "mail sent";
    }
    public void addToHistory(String subject, String groupName, Set<String> attachmentName)
    {
        UserDetails userDetails=(UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username=userDetails.getUsername();
        ZonedDateTime zonedDateTime =ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
        DateTimeFormatter date = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        DateTimeFormatter time = DateTimeFormatter.ofPattern("HH:mm:ss");
        PreviousMail previousMail=previousMailRepo.save(new PreviousMail(null,subject,groupName, zonedDateTime.format(date), zonedDateTime.format(time),null,userRepository.findByUsername(username).get()));
        List<Attachments> attachments =new ArrayList<>();
        for(String attachment:attachmentName)
        {
            attachments.add(new Attachments(null,attachment,previousMail));
        }
        attachmentRepo.saveAll(attachments);
    }
    public void saveFile(String imageDirectory, MultipartFile file, String name) throws IOException, NullPointerException {
        makeDirectoryIfNotExist(imageDirectory);
        String[] fileFrags = file.getOriginalFilename().split("\\.");
        String extension = fileFrags[fileFrags.length-1];
        Path fileNamePath = Paths.get(imageDirectory,
                name.concat(".").concat(extension));
        System.out.println(fileNamePath);
        Files.write(fileNamePath, file.getBytes());
    }
    private void makeDirectoryIfNotExist(String imageDirectory) throws IOException {
        if (!Files.exists(Paths.get(imageDirectory))) {
            Files.createDirectory(Paths.get(imageDirectory));
        }
    }

    public List<PreviousMail> showPreviousMail(Principal principal) {
        if(userRepository.findByUsername(principal.getName()).isEmpty())
            throw new UsernameNotFoundException("User not found");
        List<PreviousMail> previousMail = new ArrayList<>(userRepository.findByUsername(principal.getName()).get().getPreviousMails());
        previousMail.sort((PreviousMail a, PreviousMail b) -> a.getId() < b.getId() ? 1 : -1);
        return previousMail;
    }

    public String sendBulkWithName(NameReq nameReq) throws MessagingException, IOException, TemplateException {
        long start=System.currentTimeMillis();
        if (groupRepo.findById(nameReq.getGroupId()).isEmpty())
            throw new NoSuchElementException("Group not found");
        Groups groups = groupRepo.findById(nameReq.getGroupId()).get();
        Iterator itr = groups.getEmails().iterator();

        while (itr.hasNext())
        {
            Emails emails = (Emails) itr.next();
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
            emailConfig.setDirectoryForTemplateLoading(new File(directory));
            mimeMessageHelper.setFrom(String.valueOf(new InternetAddress("loadingerror144@gmail.com")),nameReq.getFrom());
            //TODO: throw an exception for template not found
            Template template = emailConfig.getTemplate(templateRepo.findById(nameReq.getTemplateId()).get().getName());
            mimeMessageHelper.setSubject(nameReq.getSubject());
            Map<String,String> model = new HashMap<>();
            model.put("name",emails.getName());
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template,model);
            mimeMessageHelper.setText(html, true);
            if(nameReq.getLogo()!=null)
                mimeMessageHelper.addInline("image", new FileSystemResource(new File(directory+"/"+nameReq.getLogo())));
            if(nameReq.getAttachment()!=null)
            {
                for(String attachment:nameReq.getAttachment()){
                    mimeMessageHelper.addAttachment(attachment,new File(directory+"/"+attachment));
                }
            }
            String pdfName="";
            if (nameReq.getPdfName()!=null)
            {
                pdfName=html2pdf(emails.getName(),nameReq.getPdfName());
                mimeMessageHelper.addAttachment(pdfName,new File(directory+"/"+pdfName));
            }
            log.info("Mail sent -{} {}",emails.getName(),emails.getEmail());
            mimeMessageHelper.setTo(emails.getEmail());
            mailSender.send(message);

        }
        long end=System.currentTimeMillis();
        log.info("Time -{}",end-start);
        addToHistory(nameReq.getSubject(),groups.getName(),new HashSet<>(nameReq.getAttachment()));
        return "Mail sent";

    }

    public String removeTemplate(Long templateId) {
        if(!templateRepo.existsById(templateId))
            throw new EntityNotFoundException("Template not found");
        templateRepo.deleteById(templateId);
        return "Template deleted";
    }
    public String html2pdf(String name,String fileName) throws IOException, TemplateException {
        emailConfig.setDirectoryForTemplateLoading(new File(directory));
        Template template = emailConfig.getTemplate(fileName);
        Map<String, String> model = new HashMap<>();
        model.put("name",name);
        String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
        String fileName1 = fileName.substring(0,fileName.lastIndexOf('.'))+".pdf";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            baos = generatePdf(html);
        } catch (DocumentException e) {
            System.out.println("Pdf not created");
            e.printStackTrace();
        }
        Files.write(Path.of(directory+"/"+fileName1),baos.toByteArray());
        return fileName1;
    }
    public ByteArrayOutputStream generatePdf(String html) throws DocumentException, IOException {

        try {
            String pdfFilePath = "";
            PdfWriter pdfWriter = null;
            Document document = new Document();


            document = new Document();
            document.setPageSize(PageSize.A4);
            document.addTitle("Test Pdf");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, baos);

            document.open();
            XMLWorkerHelper xmlWorkerHelper = XMLWorkerHelper.getInstance();
            xmlWorkerHelper.getDefaultCssResolver(true);
            xmlWorkerHelper.parseXHtml(pdfWriter, document, new StringReader(
                    html));


            document.close();
            System.out.println("PDF generated successfully");
            return baos;
        }
        catch (RuntimeWorkerException e)
        {
            throw new RuntimeWorkerException("Invalid nested tag head found, expected closing tag link.");
        }
    }
}