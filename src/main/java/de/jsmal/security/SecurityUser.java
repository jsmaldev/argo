package de.jsmal.security;

import de.jsmal.core.ServletEngine;
import de.jsmal.core.searchObject.SearchQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
public class SecurityUser implements UserDetails {

    private String userName;
    ServletEngine servletEngine;

    public SecurityUser(String userName, ServletEngine servletEngine) {
        this.userName = userName;
        this.servletEngine = servletEngine;
        log.info("CONSTRUCTOR COMPLETE");
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(() -> "read");
    }

    @Override
    public String getPassword() {
        return "{noop}password";
    }

    @Override
    public String getUsername() {
        SearchQuery query = new SearchQuery();
        query.setTable("sysuser");
        query.setColumns(new ArrayList<>(Arrays.asList("username", "password")));
        query.setFields(new ArrayList<>(Arrays.asList("username")));
        query.setValues(new ArrayList<>(Arrays.asList("="+this.userName)));

        query.setReturnAllColumns(false);

        //log.info("this.servletEngine = " + this.servletEngine);
        String jsonResult = this.servletEngine.search(query);
        log.info("jsonResult = " + jsonResult);
        return "admin";
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
