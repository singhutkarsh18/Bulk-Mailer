package com.example.bulkmailer.Controller;

import com.example.bulkmailer.Entities.DTOs.OTP;
import com.example.bulkmailer.Entities.DTOs.PasswordDto;
import com.example.bulkmailer.Entities.RegistrationRequest;
import com.example.bulkmailer.Services.AppUserService;
import com.example.bulkmailer.Services.RegisterService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController @AllArgsConstructor
@RequestMapping("/signup")@Slf4j
@CrossOrigin("*")
public class SignUpController {

    private RegisterService registerService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegistrationRequest request )
    {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(registerService.signUp(request));
        }
        catch (IllegalStateException e)
        {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(e.getLocalizedMessage());
        }
    }
    @PostMapping("/verifyOtp")
    public ResponseEntity<?> verifyOtp(@RequestBody OTP otp)
    {
        try{
            if(registerService.verifyAcc(otp.getUserOtp(), otp.getUsername()))
                return ResponseEntity.status(HttpStatus.ACCEPTED).body("User verified");
            else
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect OTP");
        }
        catch (NullPointerException n)
        {
            log.warn(n.getLocalizedMessage());
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Null");
        }
        catch ( UsernameNotFoundException e)
        {
            log.warn(e.getLocalizedMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getLocalizedMessage());
        }
    }
    @PostMapping("/setPassword")
    public ResponseEntity<?> setPassword(@RequestBody PasswordDto passwordDto)
    {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(registerService.createPassword(passwordDto.getUsername(), passwordDto.getPassword()));
        }
        catch(UsernameNotFoundException e1)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e1.getLocalizedMessage());
        }
        catch (IllegalStateException e2)
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e2.getLocalizedMessage());
        }
    }
    @PostMapping("/resend")
    public ResponseEntity<?> resend(@RequestBody Map<String,String> username)
    {
        try{
            return ResponseEntity.status(HttpStatus.OK).body(registerService.resendOtp(username.get("username")));
        }
        catch (UsernameNotFoundException e)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getLocalizedMessage());
        }
    }
    @PostMapping("/forgot")
    public ResponseEntity<?> forgot(@RequestBody Map<String,String> username)
    {
        try{
            return ResponseEntity.status(HttpStatus.OK).body(registerService.forgotPassword(username.get("username")));
        }
        catch (UsernameNotFoundException e)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getLocalizedMessage());
        }
        catch (IllegalStateException e1)
        {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(e1.getLocalizedMessage());

        }
    }
    @GetMapping("/hello")
    public String hello()
    {
        return "Hello";
    }

}
