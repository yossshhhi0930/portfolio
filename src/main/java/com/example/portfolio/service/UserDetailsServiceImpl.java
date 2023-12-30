package com.example.portfolio.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.example.portfolio.entity.User;
import com.example.portfolio.entity.User.Authority;
import com.example.portfolio.repository.UserRepository;

import lombok.val;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


    @Service
    public class UserDetailsServiceImpl implements UserDetailsService {

        @Autowired
        private UserRepository repository;
        
        protected static Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

        @Override
        @Transactional
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

            if (username == null || "".equals(username)) {
                throw new UsernameNotFoundException("Username is empty");
            }

            User entity = repository.findByUsername(username);
            if (entity == null) {
                throw new UsernameNotFoundException("User not found with username: " + username);
            }

            return buildUserDetails(entity);
        }

        private UserDetails buildUserDetails(User user) {
            return org.springframework.security.core.userdetails.User
                    .withUsername(user.getUsername())
                    .password(user.getPassword())
                    .authorities(mapAuthorities(user.getAuthority()))
                    .disabled(!user.isEnabled())
                    .accountExpired(!user.isAccountNonExpired())
                    .accountLocked(!user.isAccountNonLocked())
                    .credentialsExpired(!user.isCredentialsNonExpired())
                    .build();
        }

        private Collection<? extends GrantedAuthority> mapAuthorities(User.Authority authority) {
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority(authority.toString()));
            return authorities;
        }
    }