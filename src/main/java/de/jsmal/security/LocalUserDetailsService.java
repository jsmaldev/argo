package de.jsmal.security;

import de.jsmal.core.ServletEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


public class LocalUserDetailsService implements UserDetailsService {

    @Autowired
    ServletEngine servletEngine;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return new SecurityUser(username, servletEngine);
    }
}
