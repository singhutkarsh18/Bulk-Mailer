package com.example.bulkmailer.Controller;

import com.example.bulkmailer.Entities.AppUser;
import com.example.bulkmailer.Entities.DTOs.GoogleRequest;
import com.example.bulkmailer.Entities.DTOs.OTP;
import com.example.bulkmailer.Entities.DTOs.PasswordChangeDTO;
import com.example.bulkmailer.Entities.DTOs.PasswordDto;
import com.example.bulkmailer.Entities.RegistrationRequest;
import com.example.bulkmailer.Entities.Role;
import com.example.bulkmailer.JWT.JwtUtil;
import com.example.bulkmailer.Repository.GroupRepo;
import com.example.bulkmailer.Repository.UserRepository;
import com.example.bulkmailer.Services.RegisterService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController @AllArgsConstructor
@RequestMapping("/signup")@Slf4j
@CrossOrigin("*")
public class SignUpController {

    private RegisterService registerService;
    private UserRepository userRepository;
    private GroupRepo groupRepo;
    private UserDetailsService userDetailsService;
    private JwtUtil jwtUtil;

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
    public ResponseEntity<?> googleSignIn(@RequestBody HashMap<String,String> token) throws GeneralSecurityException, IOException {
        String CLIENT_ID="852195797172-d0qq3vi9erb2ep1ill5eilc65mdvmah9.apps.googleusercontent.com";
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(CLIENT_ID))
                .build();
        String idTokenString= token.get("token");
        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken != null) {
            Payload payload = idToken.getPayload();
            String userId = payload.getSubject();
            System.out.println("User ID: " + userId);
            String email = payload.getEmail();
            boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
            String name = (String) payload.get("name");
            if(emailVerified)
            {
                final UserDetails userDetails;
                Optional<AppUser> appUser=userRepository.findByUsername(email);
                if(appUser.isPresent()&&appUser.get().getRole().equals(Role.NORMAL_USER))
                {
                    return ResponseEntity.status(HttpStatus.ALREADY_REPORTED).body("User already present");
                }
                else if(appUser.isPresent()&&appUser.get().getRole().equals(Role.GOOGLE_USER))
                {
                userDetails = userDetailsService.loadUserByUsername(email);
                }
                else
                {
                    AppUser appUser1 = new AppUser(name,email,registerService.generatePassayPassword(),0,Role.GOOGLE_USER);
                    appUser1.setEnabled(true);
                    appUser1.setLocked(false);
                 userRepository.save(appUser1);
                 userDetails = userDetailsService.loadUserByUsername(email);
                }
                final String access_token= jwtUtil.generateAccessToken(userDetails);
                final String refresh_token= jwtUtil.generateRefreshToken(userDetails);
                Map<String,String> responseToken = new HashMap<>();
                responseToken.put("access_token",access_token);
                responseToken.put("refresh_token",refresh_token);
                return ResponseEntity.status(HttpStatus.OK).body(responseToken);
            }
            else
            {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not verified");
            }
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid ID token.");
        }

    }
    @PostMapping("/changePassword")
    public ResponseEntity<?> changePassword(@RequestBody PasswordChangeDTO passwordChangeDTO, Principal principal)
    {
        try{
            return ResponseEntity.status(HttpStatus.OK).body(registerService.changePassword(passwordChangeDTO,principal));
        }
        catch(UsernameNotFoundException e1)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e1.getLocalizedMessage());
        }
        catch (IllegalArgumentException e2)
        {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e2.getLocalizedMessage());
        }
        catch (IllegalStateException e3)
        {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(e3.getLocalizedMessage());
        }
        catch (RuntimeException e4)
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e4.getLocalizedMessage());
        }
    }
}
