package com.oneDev.ecommerce.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.oneDev.ecommerce.entity.Role;
import com.oneDev.ecommerce.entity.User;
import com.oneDev.ecommerce.enumaration.ExceptionType;
import com.oneDev.ecommerce.exception.ApplicationException;
import com.oneDev.ecommerce.model.UserInfo;
import com.oneDev.ecommerce.repository.RoleRepository;
import com.oneDev.ecommerce.repository.UserRepository;
import com.oneDev.ecommerce.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CacheService cacheService;


    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        String USER_CACHE_KEY = "cache:user:";
        String USER_ROLES_CACHE_KEY = "cache:user:roles:";

        String userCacheKey = USER_CACHE_KEY + usernameOrEmail;
        String rolesCacheKey = USER_ROLES_CACHE_KEY + usernameOrEmail;

        Optional<User> userOpt = cacheService.get(userCacheKey, User.class);
        Optional<List<Role>> rolesOpt = cacheService.get(rolesCacheKey, new TypeReference<List<Role>>(){
        });

        if (userOpt.isPresent() && rolesOpt.isPresent()) {
            return UserInfo.builder()
                    .user(userOpt.get())
                    .roles(rolesOpt.get())
                    .build();
        }

        log.info("Attempting to load user by username or email: {}", usernameOrEmail);
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail)

                .orElseThrow(() -> new ApplicationException(ExceptionType.USER_NOT_FOUND, "User not found with: " + usernameOrEmail));


        List<Role> roles = roleRepository.findByUserId(user.getUserId());
         UserInfo userInfo = UserInfo.builder()
                .user(user)
                .roles(roles)
                .build();

         cacheService.put(userCacheKey, user);
         cacheService.put(rolesCacheKey, roles);
         return userInfo;
    }
}
