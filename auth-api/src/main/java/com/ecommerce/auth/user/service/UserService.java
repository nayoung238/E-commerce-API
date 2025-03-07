package com.ecommerce.auth.user.service;

import com.ecommerce.auth.auth.enums.BaseRole;
import com.ecommerce.auth.user.dto.response.UserResponse;
import com.ecommerce.auth.user.dto.request.SignUpRequest;
import com.ecommerce.auth.user.entity.User;
import com.ecommerce.auth.user.repository.UserRepository;
import com.ecommerce.auth.common.exception.CustomException;
import com.ecommerce.auth.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserResponse createUser(SignUpRequest request) {
        if(userRepository.existsByLoginId(request.loginId())) {
            throw new CustomException(ErrorCode.DUPLICATE_LOGIN_ID);
        }

        try {
            User user = User.of(request);
            userRepository.save(user);
            return UserResponse.of(user);
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ErrorCode.DUPLICATE_LOGIN_ID);
        }
    }

    public UserResponse findUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

        return UserResponse.of(user);
    }

    public User findUserEntity(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));
    }

    public User findUserEntity(String loginId) {
        return userRepository.findByLoginId(loginId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));
    }

    public BaseRole getRole(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

        return user.getRole();
    }
}
