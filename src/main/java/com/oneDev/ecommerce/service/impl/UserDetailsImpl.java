package com.oneDev.ecommerce.service.impl;

import com.oneDev.ecommerce.entity.Role;
import com.oneDev.ecommerce.entity.User;
import com.oneDev.ecommerce.enumaration.ExceptionType;
import com.oneDev.ecommerce.exception.ApplicationException;
import com.oneDev.ecommerce.model.UserInfo;
import com.oneDev.ecommerce.repository.RoleRepository;
import com.oneDev.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Loading user by username: {}", username);
        User user = userRepository.findByKeyword(username)
                .orElseThrow(() -> new ApplicationException(ExceptionType.USER_NOT_FOUND, "User not found with: " + username));

        log.info("User found: {}", user);
        List<Role> roles = roleRepository.findByUserId(user.getUserId());
        return UserInfo.builder()
                .user(user)
                .roles(roles)
                .build();
    }
}
