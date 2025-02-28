package com.ecommerce.auth.user.service;

import com.ecommerce.auth.user.dto.UserResponseDto;
import com.ecommerce.auth.user.dto.SignUpRequestDto;
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
    public UserResponseDto createUser(SignUpRequestDto request) {
        if(userRepository.existsByLoginId(request.loginId())) {
            throw new CustomException(ErrorCode.DUPLICATE_LOGIN_ID);
        }

        try {
            User user = User.of(request);
            userRepository.save(user);
            return UserResponseDto.of(user);
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ErrorCode.DUPLICATE_LOGIN_ID);
        }
    }

    public UserResponseDto findUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

        return UserResponseDto.of(user);
    }
}
