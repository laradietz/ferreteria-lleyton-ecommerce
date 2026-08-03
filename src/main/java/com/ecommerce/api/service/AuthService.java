package com.ecommerce.api.service;

import com.ecommerce.api.dto.*;
import com.ecommerce.api.entity.Cart;
import com.ecommerce.api.entity.RoleName;
import com.ecommerce.api.entity.User;
import com.ecommerce.api.exception.BusinessException;
import com.ecommerce.api.repository.CartRepository;
import com.ecommerce.api.repository.UserRepository;
import com.ecommerce.api.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public UserResponse register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new BusinessException("Ese nombre de usuario ya está en uso");
        }
        if (userRepository.existsByEmail(req.email())) {
            throw new BusinessException("Ese email ya está registrado");
        }

        User user = User.builder()
                .username(req.username())
                .email(req.email())
                .passwordHash(passwordEncoder.encode(req.password()))
                .firstName(req.firstName())
                .lastName(req.lastName())
                .phone(req.phone())
                .address(req.address())
                .active(true)
                .role(RoleName.CUSTOMER)
                .build();
        user = userRepository.save(user);

        Cart cart = Cart.builder().user(user).build();
        cartRepository.save(cart);

        return toResponse(user);
    }

    public LoginResponse login(LoginRequest req) {
        User user = userRepository.findByUsername(req.username())
                .orElseThrow(() -> new BusinessException("Usuario o contraseña incorrectos"));

        if (!user.getActive()) {
            throw new BusinessException("Este usuario está inactivo");
        }
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new BusinessException("Usuario o contraseña incorrectos");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        return new LoginResponse(token, user.getUsername(), user.getRole().name(), jwtUtil.getExpirationMs());
    }

    private UserResponse toResponse(User u) {
        return new UserResponse(u.getId(), u.getUsername(), u.getEmail(), u.getFirstName(),
                u.getLastName(), u.getPhone(), u.getAddress(), u.getActive(),
                u.getRole().name(), u.getCreatedAt());
    }
}
