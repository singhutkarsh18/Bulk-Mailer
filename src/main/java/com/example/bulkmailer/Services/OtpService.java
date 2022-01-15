package com.example.bulkmailer.Services;

import com.example.bulkmailer.Entities.DTOs.Mail;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service@AllArgsConstructor@Slf4j
public class OtpService {

    @Autowired
    private JavaMailSender javaMailSender;


    public void sendMail(Mail mail) {

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(mail.getRecipient());
        msg.setSubject(mail.getSubject());
        msg.setText(mail.getMessage());
        this.javaMailSender.send(msg);
        log.info("Otp sent - {}", new Date());
    }

//    public void sendMail(String subject,String recipient,String body) {
//        Email from = new Email("test@example.com");
//        String subject1 = subject;
//        Email to = new Email(recipient);
//        Content content = new Content("text/plain", body);
//        Mail mail = new Mail(from, subject, to, content);
//
//        Request request = new Request();
//        try {
//            request.setMethod(Method.POST);
//            request.setEndpoint("mail/send");
//            request.setBody(mail.build());
//            Response response = this.sendGrid.api(request);
//            sendGrid.api(request);
//            log.info("Otp sent - {}", new Date());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }


    private static final Integer EXPIRE_MINS = 5;

    private LoadingCache<String, Integer> otpCache;

    public OtpService(){
        super();
        otpCache = CacheBuilder.newBuilder().expireAfterWrite(EXPIRE_MINS, TimeUnit.MINUTES).build(new CacheLoader<String, Integer>() {
            public Integer load(String key) {
                return 0;
            }
        });
    }
    public int generateOTP(String key){

        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        otpCache.put(key, otp);
        return otp;
    }
    public int getOtp(String key){
        try{
            return otpCache.get(key);
        }catch (Exception e){
            return 0;
        }
    }
    public void clearOTP(String key){
        otpCache.invalidate(key);
    }
    public boolean otpExpired(int otp,String key)
    {
        if(otp==this.getOtp(key))
        {
            return true;
        }
        else
            return false;
    }
}
