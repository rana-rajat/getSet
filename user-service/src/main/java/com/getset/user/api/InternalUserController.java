package com.getset.user.api;

import com.getset.common.dto.UserSummaryDto;
import com.getset.common.exception.NotFoundException;
import com.getset.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Internal endpoint called by other microservices via Feign Client.
 * NOT exposed externally (API Gateway does not route /internal/** to clients).
 */
@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

        private final UserRepository userRepository;

        @GetMapping("/{id}")
        public UserSummaryDto getUserById(@PathVariable String id) {
                // Support both MongoDB ObjectId and email lookups (JWT subject is email)
                var user = id.contains("@")
                                ? userRepository.findByEmail(id)
                                                .orElseThrow(() -> new NotFoundException("User not found: " + id))
                                : userRepository.findById(id)
                                                .orElseThrow(() -> new NotFoundException("User not found: " + id));
                return UserSummaryDto.builder()
                                .id(user.getId()).name(user.getName())
                                .email(user.getEmail()).role(user.getRole().name())
                                .phone(user.getPhone()).build();
        }

        @GetMapping("/by-email/{email}")
        public UserSummaryDto getUserByEmail(@PathVariable String email) {
                return userRepository.findByEmail(email)
                                .map(u -> UserSummaryDto.builder()
                                                .id(u.getId()).name(u.getName())
                                                .email(u.getEmail()).role(u.getRole().name())
                                                .phone(u.getPhone()).build())
                                .orElseThrow(() -> new NotFoundException("User not found: " + email));
        }
}
