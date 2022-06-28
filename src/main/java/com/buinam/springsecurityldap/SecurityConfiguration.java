package com.buinam.springsecurityldap;

import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter  {

    //tell spring security to use ldap to authenticate users
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.ldapAuthentication()
                .userDnPatterns("uid={0},ou=people") // Dn stands for distinguished name. It's basically a way to identify a user. User information is stored in ldif format
                .groupSearchBase("ou=groups") // tell the organization unit to search for is groups
                .contextSource()
                .url("ldap://localhost:8389/dc=springframework,dc=org") // url of the ldap server hosted on localhost:8389
                .and()
                .passwordCompare() // tell the ldap server to compare passwords
                .passwordEncoder(new BCryptPasswordEncoder()) // tell the ldap server to use bcrypt to hash passwords
                .passwordAttribute("userPassword"); // tell spring security to use the userPassword attribute to authenticate users
    }


    // authorize any request to the application
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .anyRequest().fullyAuthenticated()
                .and()
                .formLogin();
    }
}
