package com.example.bulkmailer.Services;

import com.example.bulkmailer.Entities.AppUser;
import com.example.bulkmailer.Entities.RegistrationRequest;
import com.example.bulkmailer.Repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;
@Service@AllArgsConstructor@Slf4j
public class RegisterService {

    private UserRepository userRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private final String emailRegex="^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[\\a-zA-Z]{2,6}";
    private final String passwordRegex="^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";

    public String signUp(RegistrationRequest request) {
        log.info("Email - {}",request.getUsername());
        log.info("Password - {}",request.getPassword());
        log.info("Valid email - {}",email_password_Validator(emailRegex,request.getUsername()));
        log.info("Valid Password - {}",email_password_Validator(passwordRegex,request.getPassword()));

        if(!email_password_Validator(emailRegex,request.getUsername()))
            throw new IllegalStateException("Invalid email");
        if(!email_password_Validator(passwordRegex,request.getPassword()))
            throw new IllegalStateException("Invalid Password");

        if(userRepository.findByUsername(request.getUsername()).isPresent())
            throw new IllegalStateException("Email already present");

        String encodedPassword= bCryptPasswordEncoder.encode(request.getPassword());
        AppUser appUser=new AppUser(request.getName(),request.getUsername(),encodedPassword);
        userRepository.save(appUser);
        return "works";
    }

    public Boolean email_password_Validator(String regex,String value)
    {
        Pattern pattern=Pattern.compile(regex);
        if(value==null)
            return false;
        return pattern.matcher(value).matches();
    }
}
