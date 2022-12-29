package de.jsmal.security;

import de.jsmal.core.ServletEngine;
import de.jsmal.core.engine.search.ResultSearchList;
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
    private String password;
    ServletEngine servletEngine;

    public SecurityUser(String userName, ServletEngine servletEngine) {
        this.userName = userName;
        this.password = "-1";
        this.servletEngine = servletEngine;

        // get user from DB
        SearchQuery query = new SearchQuery();
        query.setTable("sysuser");
        query.setColumns(new ArrayList<>(Arrays.asList("username", "password", "is_active")));
        query.setFields(new ArrayList<>(Arrays.asList("username")));
        query.setValues(new ArrayList<>(Arrays.asList("="+this.userName)));

        query.setReturnAllColumns(false);

        //log.info("this.servletEngine = " + this.servletEngine);
        //String jsonResult = this.servletEngine.search(query);
        ResultSearchList userFromDB_ResultSearchList = this.servletEngine.searchList(query);
//        log.info("RET_CODE = " + userFromDB_ResultSearchList.getRetCode());
//        log.info("count_values = " + userFromDB_ResultSearchList.getCount_values());
//        log.info("requested_columns = " + userFromDB_ResultSearchList.getRequestedColumns());
        log.info("username = " + userFromDB_ResultSearchList.getObjects().get(0).getAttributeValueByName("username"));
        log.info("password = " + userFromDB_ResultSearchList.getObjects().get(0).getAttributeValueByName("password"));
//        log.info("is_active = " + userFromDB_ResultSearchList.getObjects().get(0).getAttributeValueByName("is_active"));


        if (userFromDB_ResultSearchList.getCount_values() == 1) {
            //success login
            //log.info("is_active = " +userFromDB_ResultSearchList.getObjects().get(0).getAttributeValueByName("is_active"));
            // IF user is active - check the password
            if ((Boolean)userFromDB_ResultSearchList.getObjects().get(0).getAttributeValueByName("is_active")) {
                this.password = (String)userFromDB_ResultSearchList.getObjects().get(0).getAttributeValueByName("password");
            }
        }
        //log.info("CONSTRUCTOR COMPLETE");
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(() -> "read");
    }

    @Override
    public String getPassword() {
//        log.info("getPassword: " + this.password);
//        return "{noop}"+this.password;
        return "{bcrypt}"+this.password;
    }

    @Override
    public String getUsername() {
//        log.info("getUsername: " + this.userName);
        return this.userName;
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
