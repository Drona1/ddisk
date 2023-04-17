package com.gmail.dimabah.ddisk.services;

import com.gmail.dimabah.ddisk.models.DiskUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final DiskUserService diskUserService;

    public UserDetailsServiceImpl(DiskUserService diskUserService) {
        this.diskUserService = diskUserService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        DiskUser user = diskUserService.findByEmail(username);

        if (user == null) {
            throw new UsernameNotFoundException(username + " not found!");
        }

        List<GrantedAuthority> roles = List.of(new SimpleGrantedAuthority(user.getRole().toString()));

        return new User(user.getEmail(), user.getPass(), roles);
    }
}
