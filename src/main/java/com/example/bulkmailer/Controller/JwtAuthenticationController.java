package com.example.bulkmailer.Controller;

import com.example.bulkmailer.Entities.AppUser;
import com.example.bulkmailer.JWT.JwtRequest;
import com.example.bulkmailer.JWT.JwtResponse;
import com.example.bulkmailer.JWT.JwtUtil;
import com.example.bulkmailer.Repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.index.qual.SameLen;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")@Slf4j
@AllArgsConstructor
public class JwtAuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/authenticate")
    public ResponseEntity<?> createStudentAuthenticationToken(@RequestBody JwtRequest authenticationRequest) {

        String auth =authenticateStudent(authenticationRequest.getUsername(), authenticationRequest.getPassword());
        if(auth.equals("true")) {
            final UserDetails userDetails = userDetailsService
                    .loadUserByUsername(authenticationRequest.getUsername());

            final String access_token = jwtUtil.generateAccessToken(userDetails);
            final String refresh_token=jwtUtil.generateRefreshToken(userDetails);

            Map<String,String> token=new HashMap<>();
            token.put("access_token",access_token);
            token.put("refresh_token",refresh_token);
            return ResponseEntity.ok(token);
        }
        else
        {
            return new ResponseEntity<>(auth, HttpStatus.OK);
        }
    }

    private String authenticateStudent(String username, String password)  {

        try {
            AppUser appUser = userRepository.findByUsername(username).get();
            if (passwordEncoder.matches(password, appUser.getPassword())) {
                return "true";
            } else {
                System.out.println(username);
                System.out.println(passwordEncoder.matches(password, appUser.getPassword()));
                return "false";
            }
        }
        catch(ExpiredJwtException e1)
        {
            log.info(e1.getLocalizedMessage());
            return "JWT token has expired";
        }
        catch (Exception e)
        {
            System.out.println(e.getLocalizedMessage());
            return "User not found";
        }
    }


}