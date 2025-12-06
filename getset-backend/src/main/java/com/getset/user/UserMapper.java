package com.getset.user;

import com.getset.auth.dto.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    
    public UserResponse toResponse(UserDocument user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .phone(user.getPhone())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
