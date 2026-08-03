package com.ecommerce.api.service;

import com.ecommerce.api.dto.UpdateMyAccountRequest;
import com.ecommerce.api.dto.UserResponse;
import com.ecommerce.api.entity.User;
import com.ecommerce.api.exception.BusinessException;
import com.ecommerce.api.exception.ResourceNotFoundException;
import com.ecommerce.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse getMyAccount(String username) {
        return toResponse(findByUsername(username));
    }

    @Transactional
    public UserResponse updateMyAccount(String username, UpdateMyAccountRequest req) {
        User user = findByUsername(username);

        if (!passwordEncoder.matches(req.currentPassword(), user.getPasswordHash())) {
            throw new BusinessException("La contraseña actual no es correcta");
        }
        if (req.newUsername() != null && !req.newUsername().isBlank()
                && !req.newUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(req.newUsername())) {
                throw new BusinessException("Ese nombre de usuario ya está en uso");
            }
            user.setUsername(req.newUsername());
        }
        if (req.newPassword() != null && !req.newPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        }
        if (req.firstName() != null && !req.firstName().isBlank()) user.setFirstName(req.firstName());
        if (req.lastName() != null && !req.lastName().isBlank()) user.setLastName(req.lastName());
        if (req.phone() != null) user.setPhone(req.phone());
        if (req.address() != null) user.setAddress(req.address());

        return toResponse(userRepository.save(user));
    }

    private User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", username));
    }

    private UserResponse toResponse(User u) {
        return new UserResponse(u.getId(), u.getUsername(), u.getEmail(), u.getFirstName(),
                u.getLastName(), u.getPhone(), u.getAddress(), u.getActive(),
                u.getRole().name(), u.getCreatedAt());
    }
}
