package com.oneDev.ecommerce.service.impl;

import com.oneDev.ecommerce.entity.Role;
import com.oneDev.ecommerce.entity.User;
import com.oneDev.ecommerce.entity.UserRole;
import com.oneDev.ecommerce.enumaration.ExceptionType;
import com.oneDev.ecommerce.exception.ApplicationException;
import com.oneDev.ecommerce.model.request.UserRegisterRequest;
import com.oneDev.ecommerce.model.request.UserUpdateRequest;
import com.oneDev.ecommerce.model.response.UserResponse;
import com.oneDev.ecommerce.repository.RoleRepository;
import com.oneDev.ecommerce.repository.UserRepository;
import com.oneDev.ecommerce.repository.UserRoleRepository;
import com.oneDev.ecommerce.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override @Transactional
    public UserResponse register(UserRegisterRequest userRegisterRequest) {

        if(existsByUserName(userRegisterRequest.getUsername())) {
            throw new ApplicationException(ExceptionType.USERNAME_ALREADY_EXISTS);
        }
        if (existsByEmail(userRegisterRequest.getEmail())) {
            throw new ApplicationException(ExceptionType.EMAIL_ALREADY_EXISTS);
        }
        if (!userRegisterRequest.getPassword().equals(userRegisterRequest.getPasswordConfirmation())){
            throw new ApplicationException(ExceptionType.BAD_REQUEST,
                    ExceptionType.BAD_REQUEST.getFormattedMessage("Passwords do not match"));
        }

        String encodedPassword = passwordEncoder.encode(userRegisterRequest.getPassword());

        User user = User.builder()
                .username(userRegisterRequest.getUsername())
                .email(userRegisterRequest.getEmail())
                .enabled(true)
                .password(encodedPassword)
                .build();

        userRepository.save(user);
        Role role = roleRepository.findByName("ROLE_ADMIN").orElseThrow(
                () -> new ApplicationException(ExceptionType.ROLE_NOT_FOUND)
        );
        UserRole userRoleRelation = UserRole.builder()
                .userRoleId(new UserRole.UserRoleId(user.getUserId(), role.getRoleId()))
                .build();

        userRoleRepository.save(userRoleRelation);
        return UserResponse.from(user, List.of(role));
    }

    @Override
    public UserResponse findById(Long userId) {
        User user = userRepository.findById(userId).
                orElseThrow(() -> new ApplicationException(ExceptionType.USER_NOT_FOUND,
                        ExceptionType.USER_NOT_FOUND.getFormattedMessage("With id: " + userId)));
        List<Role> userRoles = roleRepository.findByUserId(userId);
        return UserResponse.from(user, userRoles);
    }

    @Override
    public UserResponse findByKeyword(String keyword) {
        User user = userRepository.findByKeyword(keyword).
                orElseThrow(() -> new ApplicationException(ExceptionType.USER_NOT_FOUND,
                        ExceptionType.USER_NOT_FOUND.getFormattedMessage("With username / email: " + keyword)));
        List<Role> userRoles = roleRepository.findByUserId(user.getUserId());
        return UserResponse.from(user, userRoles);
    }

    @Override @Transactional
    public UserResponse updateUser(Long userId, UserUpdateRequest userUpdateRequest) {

        User user = userRepository.findById(userId).
                orElseThrow(() -> new ApplicationException(ExceptionType.USER_NOT_FOUND,
                        ExceptionType.USER_NOT_FOUND.getFormattedMessage("With id: " + userId)));

        if (userUpdateRequest.getCurrentPassword() != null && userUpdateRequest.getNewPassword() != null) {
            if (!passwordEncoder.matches(userUpdateRequest.getCurrentPassword(), user.getPassword())) {
                throw new ApplicationException(ExceptionType.INVALID_PASSWORD);
            }

            String encodedPassword = passwordEncoder.encode(userUpdateRequest.getNewPassword());
            user.setPassword(encodedPassword);
        }

        if (userUpdateRequest.getUsername() != null && !userUpdateRequest.getUsername().equals(user.getUsername())) {
            if(existsByUserName(userUpdateRequest.getUsername())) {
                throw new ApplicationException(ExceptionType.USERNAME_ALREADY_EXISTS);
            }
            user.setUsername(userUpdateRequest.getUsername());
        }

        if (userUpdateRequest.getEmail() != null && !userUpdateRequest.getEmail().equals(user.getEmail())) {
            if(existsByEmail(userUpdateRequest.getEmail())) {
                throw new ApplicationException(ExceptionType.EMAIL_ALREADY_EXISTS);
            }
            user.setEmail(userUpdateRequest.getEmail());
        }

        userRepository.save(user);
        List<Role> roles = roleRepository.findByUserId(user.getUserId());
        return UserResponse.from(user, roles);
    }

    @Override
    public boolean existsByUserName(String userName) {
        return userRepository.existsByUsername(userName);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override @Transactional
    public void deleteById(Long userId) {
        User user = userRepository.findById(userId).
                orElseThrow(() -> new ApplicationException(ExceptionType.USER_NOT_FOUND,
                        ExceptionType.USER_NOT_FOUND.getFormattedMessage("With id: " + userId)));

        userRoleRepository.deleteByIdUserId(userId);
        userRepository.delete(user);
    }
}
