package com.dimazak.gym.security;

import com.dimazak.gym.dao.UserDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UserDao userDao;

    public CustomUserDetailsService(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        log.debug("Loading user details for username: {}", username);
        return userDao.findByUsername(username)
                .map(CustomUserDetails::new)
                .orElseThrow(() -> {
                    log.warn("User not found during authentication: {}", username);
                    return new UsernameNotFoundException("User not found: " + username);
                });
    }
}