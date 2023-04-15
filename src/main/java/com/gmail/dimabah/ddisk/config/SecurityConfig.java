package com.gmail.dimabah.ddisk.config;

import com.gmail.dimabah.ddisk.components.CustomPermissionEvaluator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final CustomPermissionEvaluator customPermissionEvaluator;

    public SecurityConfig(CustomPermissionEvaluator customPermissionEvaluator) {
        this.customPermissionEvaluator = customPermissionEvaluator;
    }
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public MethodSecurityExpressionHandler createExpressionHandler() {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setPermissionEvaluator(customPermissionEvaluator);
        return expressionHandler;
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeHttpRequests( authConfig->{
                    authConfig.requestMatchers("/register").permitAll();
                    authConfig.requestMatchers(HttpMethod.POST,"/newuser").permitAll();
                    authConfig.requestMatchers("/folders/**","/js/**").permitAll();
                    authConfig.anyRequest().authenticated();
                })
                .exceptionHandling()
                .accessDeniedPage("/unauthorized")
                .and()
                .formLogin()
                .loginPage("/login")
                .loginProcessingUrl("/d_spring_security_check")
                .failureUrl("/login?error")
                .usernameParameter("d_login")
                .passwordParameter("d_password")
                .permitAll()
                .and()
                .logout()
                .logoutUrl("/logout")
                .permitAll()
                .logoutSuccessUrl("/login?logout")
                .and()
        ;

        return http.build();
    }
}
