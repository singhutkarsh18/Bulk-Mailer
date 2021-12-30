package com.example.bulkmailer.Controller;

import com.example.bulkmailer.Entities.AppUser;
import com.example.bulkmailer.Entities.DTOs.GoogleToken;
import com.example.bulkmailer.Entities.DTOs.OTP;
import com.example.bulkmailer.Entities.DTOs.PasswordDto;
import com.example.bulkmailer.Entities.RegistrationRequest;
import com.example.bulkmailer.Repository.UserRepository;
import com.example.bulkmailer.Services.RegisterService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Map;

@RestController @AllArgsConstructor
    @RequestMapping("/signup")@Slf4j
@CrossOrigin("*")
public class SignUpController {

    private RegisterService registerService;
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegistrationRequest request )
    {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(registerService.signUp(request));
        }
        catch (IllegalStateException e)
        {
            if(e.getLocalizedMessage().equals("Invalid email"))
                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(e.getLocalizedMessage());
            else if(e.getLocalizedMessage().equals("not verified"))
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getLocalizedMessage());
            else if(e.getLocalizedMessage().equals("password not set"))
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(e.getLocalizedMessage());
            else
                return ResponseEntity.status(HttpStatus.ALREADY_REPORTED).body(e.getLocalizedMessage());

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
        catch (EntityNotFoundException e3)
        {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(e3.getLocalizedMessage());
        }
        catch (UnsupportedOperationException e4)
        {
            return ResponseEntity.status(HttpStatus.ALREADY_REPORTED).body(e4.getLocalizedMessage());
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
            if(e1.getLocalizedMessage().equals("Invalid email"))
                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(e1.getLocalizedMessage());
            else if(e1.getLocalizedMessage().equals("not verified"))
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e1.getLocalizedMessage());
            else if(e1.getLocalizedMessage().equals("password not set"))
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(e1.getLocalizedMessage());
            else
                return ResponseEntity.status(HttpStatus.ALREADY_REPORTED).body(e1.getLocalizedMessage());
        }
    }
    @PostMapping("/google")
    public ResponseEntity<?> googleSignIn(@RequestBody GoogleToken token) throws GeneralSecurityException, IOException {
        String CLIENT_ID="852195797172-d0qq3vi9erb2ep1ill5eilc65mdvmah9.apps.googleusercontent.com";
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(CLIENT_ID))
                .build();

        String idTokenString=token.getToken();
        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken != null) {
            Payload payload = idToken.getPayload();

            String userId = payload.getSubject();
            System.out.println("User ID: " + userId);

            String email = payload.getEmail();
            boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
            String name = (String) payload.get("name");

            if(!userRepository.findByUsername(email).isPresent())
                userRepository.save(new AppUser(name,email,null,0));
            if(emailVerified)
                return ResponseEntity.status(HttpStatus.CREATED).build();
            else
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("email not verified");

        } else {
            System.out.println("Invalid ID token.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid ID token.");
        }
    }
    @GetMapping("/hello")
    public String hello()
    {
        return "Hello APi";
    }

}
