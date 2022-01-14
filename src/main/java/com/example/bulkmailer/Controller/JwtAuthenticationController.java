package com.example.bulkmailer.Controller;

import com.example.bulkmailer.JWT.JwtRequest;
import com.example.bulkmailer.JWT.JwtUtil;
import com.example.bulkmailer.Repository.UserRepository;
import com.example.bulkmailer.Services.RegisterService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.impl.DefaultClaims;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")@Slf4j
@AllArgsConstructor
public class JwtAuthenticationController {


    private JwtUtil jwtUtil;


    private UserDetailsService userDetailsService;

    private UserRepository userRepository;
    private RegisterService registerService;
    @PostMapping("/authenticate")
    public ResponseEntity<?> createStudentAuthenticationToken(@RequestBody JwtRequest authenticationRequest) {

        try {
            boolean auth = registerService.authenticateStudent(authenticationRequest.getUsername(), authenticationRequest.getPassword());

                final UserDetails userDetails = userDetailsService
                        .loadUserByUsername(authenticationRequest.getUsername());

                final String access_token = jwtUtil.generateAccessToken(userDetails);
                final String refresh_token = jwtUtil.generateRefreshToken(userDetails);
                Map<String, String> token = new HashMap<>();
                token.put("access_token", access_token);
                token.put("refresh_token", refresh_token);
                return ResponseEntity.ok(token);

        }
        catch (UsernameNotFoundException e1)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e1.getLocalizedMessage());
        }
        catch (IllegalStateException e2)
        {
            if(e2.getLocalizedMessage().equals("not verified"))
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e2.getLocalizedMessage());
            else if(e2.getLocalizedMessage().equals("password not set"))
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e2.getLocalizedMessage());
            else
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e2.getLocalizedMessage());
        }
    }


    @PostMapping("/refreshToken")
    public ResponseEntity<?> refreshtoken(HttpServletRequest request) {
        try {
            DefaultClaims claims = (io.jsonwebtoken.impl.DefaultClaims) request.getAttribute("claims");
            String refreshToken = request.getHeader("refresh_token");
            if (userRepository.findByUsername(jwtUtil.getUsernameFromToken(refreshToken)).isEmpty())
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not present");
            UserDetails userDetails = userDetailsService.loadUserByUsername(jwtUtil.getUsernameFromToken(refreshToken));

            String access_token = jwtUtil.generateAccessToken(userDetails);
            Map<String, String> token = new HashMap<>();
            token.put("access_token", access_token);
            return ResponseEntity.ok(token);
        }
        catch(ExpiredJwtException e1)
        {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e1.getLocalizedMessage());
        }
        catch (SignatureException e2)
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e2.getLocalizedMessage());
        }
    }



}