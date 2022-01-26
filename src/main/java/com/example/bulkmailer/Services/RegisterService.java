package com.example.bulkmailer.Services;

import com.example.bulkmailer.Entities.AppUser;
import com.example.bulkmailer.Entities.DTOs.Mail;
import com.example.bulkmailer.Entities.DTOs.PasswordChangeDTO;
import com.example.bulkmailer.Entities.RegistrationRequest;
import com.example.bulkmailer.JWT.JwtUtil;
import com.example.bulkmailer.Repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.auth.InvalidCredentialsException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
@Service@AllArgsConstructor@Slf4j
public class RegisterService {

    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder;

    private final String emailRegex="^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[\\a-zA-Z]{2,6}";

    private final String passwordRegex="^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";

    private OtpService otpService;

    private UserDetailsService userDetailsService;

    private JwtUtil jwtUtil;

    public String signUp(RegistrationRequest request) {
        log.info("Email - {}",request.getUsername());
        log.info("Valid email - {}",email_password_Validator(emailRegex,request.getUsername()));

        if(!email_password_Validator(emailRegex,request.getUsername()))
            throw new IllegalStateException("Invalid email");

        if(userRepository.findByUsername(request.getUsername()).isPresent()&&
                userRepository.findByUsername((request.getUsername())).get().getEnabled()&&
                !userRepository.findByUsername((request.getUsername())).get().getLocked())
            throw new IllegalStateException("Email already present");
        if(userRepository.findByUsername(request.getUsername()).isPresent()&&
                !userRepository.findByUsername((request.getUsername())).get().getEnabled()&& userRepository.findByUsername((request.getUsername())).get().getLocked())
            throw new IllegalStateException("not verified");
        if(userRepository.findByUsername(request.getUsername()).isPresent()&&
                userRepository.findByUsername((request.getUsername())).get().getEnabled()&&userRepository.findByUsername((request.getUsername())).get().getLocked())
            throw new IllegalStateException("password not set");
        int otp = otpService.generateOTP(request.getUsername());
        AppUser appUser=new AppUser(request.getName(),request.getUsername(),null,otp);
        sendOtp(appUser);
        userRepository.save(appUser);

        return "otp sent";
    }

    //Method to validate email or password
    public Boolean email_password_Validator(String regex,String value)
    {
        Pattern pattern=Pattern.compile(regex);
        if(value==null)
            return false;
        return pattern.matcher(value).matches();
    }

//    Method to generate and send otp
    public void sendOtp(AppUser appUser)
    {

        try {
        log.info("email - {}",appUser.getUsername());
            String message = "OTP to verify your account is " + appUser.getOtp();
            Mail mail = new Mail(appUser.getUsername(), "Verify Your account", message);
            log.info("Otp sent - {}", appUser.getOtp());
            otpService.sendMail(mail);
        }
        catch (NullPointerException n)
        {
            log.info("Mail - {} {} ",appUser.getUsername(), appUser.getOtp());
        }
    }


    public Boolean verifyAcc(int userOtp,String username)
    {
        if(!userRepository.findByUsername(username).isPresent())
            throw new UsernameNotFoundException("user not found");
        try {
            Boolean validOtp;
            AppUser appUser = userRepository.findByUsername(username).get();
            if (userOtp >= 0) {
                int generatedOtp = appUser.getOtp();
                if (generatedOtp > 0) {
                    if (userOtp == generatedOtp) {
                        appUser.setEnabled(true);
                        userRepository.save(appUser);
                        validOtp = true;
                    } else {
                        validOtp = false;
                    }
                } else {
                    validOtp = false;
                }
            } else {
                validOtp = false;
            }
            System.out.println(validOtp);
            return validOtp;
        }
        catch(NullPointerException n)
        {
            log.info("UserOtp:"+userOtp);
            return false;
        }
    }
    public Map<String,String> createPassword(String username,String password)
    {
            if(userRepository.findByUsername(username).isEmpty())
                throw new UsernameNotFoundException("User not found");
            AppUser appUser = userRepository.findByUsername(username).get();


            if (!email_password_Validator(passwordRegex, password))
            {
                throw new IllegalStateException("Invalid Password");
            }
            if(!appUser.getEnabled())
            {
                throw new EntityNotFoundException("Student not verified through otp");
            }
            if(passwordEncoder.matches(password,appUser.getPassword()))
                throw new UnsupportedOperationException("new password same as old password");
            appUser.setPassword(passwordEncoder.encode(password));
            appUser.setLocked(false);
            userRepository.save(appUser);
        final UserDetails userDetails = userDetailsService
                .loadUserByUsername(appUser.getUsername());

        final String access_token= jwtUtil.generateAccessToken(userDetails);
        final String refresh_token= jwtUtil.generateRefreshToken(userDetails);
        Map<String,String> token = new HashMap<>();
        token.put("access_token",access_token);
        token.put("refresh_token",refresh_token);
        return token;
    }

    public String resendOtp(String username) {
        if(userRepository.findByUsername(username).isEmpty())
            throw new UsernameNotFoundException("User not found");

         AppUser appUser = userRepository.findByUsername(username).get();
        int otp = otpService.generateOTP(username);
        appUser.setOtp(otp);
        sendOtp(appUser);
        userRepository.save(appUser);
        return "otp sent";
    }

    public String forgotPassword(String username) {
        if(!email_password_Validator(emailRegex,username))
            throw new IllegalStateException("Invalid email");
        if(!userRepository.findByUsername(username).isPresent())
            throw new UsernameNotFoundException("User not found");
        AppUser appUser=userRepository.findByUsername(username).get();
        if(!appUser.getEnabled())
            throw new IllegalStateException("not verified");
        if (appUser.getLocked())
            throw new IllegalStateException("password not set");
        int otp = otpService.generateOTP(username);
        appUser.setOtp(otp);
        appUser.setEnabled(false);
        sendOtp(appUser);
        userRepository.save(appUser);
        return "otp sent";
    }

    public boolean authenticateStudent(String username, String password)  {
        if(!userRepository.findByUsername(username).isPresent())
            throw new UsernameNotFoundException("User Not present");
        AppUser appUser=userRepository.findByUsername(username).get();
        if (!appUser.getEnabled())
            throw new IllegalStateException("not verified");
        if (appUser.getLocked())
            throw new IllegalStateException("password not set");
        if (!passwordEncoder.matches(password, appUser.getPassword()))
            throw new IllegalStateException("incorrect password");
        return true;
    }

    public String changePassword(PasswordChangeDTO passwordChangeDTO, Principal principal){
        if(principal.getName()==null)
            throw new RuntimeException("Access token not present");
        if(userRepository.findByUsername(principal.getName()).isEmpty())
            throw new UsernameNotFoundException("User Not Found");
        AppUser appUser= userRepository.findByUsername(principal.getName()).get();
        if(!passwordEncoder.matches(passwordChangeDTO.getOldPassword(),appUser.getPassword()))
            throw new IllegalArgumentException("Old password does not match");
        if(passwordChangeDTO.getNewPassword().equals(passwordChangeDTO.getOldPassword()))
            throw new UnsupportedOperationException("New password and old password cannot be same");
        if(!email_password_Validator(passwordRegex, passwordChangeDTO.getNewPassword()))
            throw new IllegalStateException("Password not valid");
        appUser.setPassword(passwordEncoder.encode(passwordChangeDTO.getNewPassword()));
        userRepository.saveAndFlush(appUser);
        return "Password changed";
    }
}
