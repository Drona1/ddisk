package com.gmail.dimabah.ddisk.config;

import com.gmail.dimabah.ddisk.models.enums.UserRole;
import com.gmail.dimabah.ddisk.services.DiskUserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AppConfig implements WebMvcConfigurer {

    @Bean
    public CommandLineRunner start(final DiskUserService userService,
                                   final PasswordEncoder encoder) {
        return strings -> {
            userService.addUser("first@gmail.com",
                    encoder.encode("123456789"),
                    UserRole.USER);
            userService.addUser("another@gmail.com",
                    encoder.encode("123456789"),
                    UserRole.USER);
        };
    }


}
