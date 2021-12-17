package com.example.bulkmailer.Controller;

import com.example.bulkmailer.Entities.RegistrationRequest;
import com.example.bulkmailer.Services.AppUserService;
import com.example.bulkmailer.Services.RegisterService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController @AllArgsConstructor
@RequestMapping("/signup")@Slf4j
@CrossOrigin("*")
public class SignUpController {

    private AppUserService appUserService;
    private RegisterService registerService;

    @PostMapping("/register")
    public ResponseEntity<?> signup(@RequestBody RegistrationRequest request )
    {
        return ResponseEntity.status(HttpStatus.OK).body(registerService.signUp(request));
    }

    @GetMapping("/hello")
    public String hello()
    {
        return "Hello";
    }

}
