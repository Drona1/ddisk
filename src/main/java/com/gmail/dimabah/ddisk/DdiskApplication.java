package com.gmail.dimabah.ddisk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;

@SpringBootApplication
public class DdiskApplication {

    public static void main(String[] args) {
        SpringApplication.run(DdiskApplication.class, args);
    }

}
