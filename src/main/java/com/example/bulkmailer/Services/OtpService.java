package com.example.bulkmailer.Services;

import com.example.bulkmailer.Entities.Mail;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.LoadingCache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;

@Service@AllArgsConstructor
public class OtpService {

    @Autowired
    private JavaMailSender javaMailSender;

    public OtpService(JavaMailSender javaMailSender){
        this.javaMailSender=javaMailSender;
    }
    public void sendMail(Mail mail) {

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(mail.getRecipient());
        msg.setSubject(mail.getSubject());
        msg.setText(mail.getMessage());
        this.javaMailSender.send(msg);
    }
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
