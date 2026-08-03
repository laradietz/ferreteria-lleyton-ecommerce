package com.ecommerce.api.controller;

import com.ecommerce.api.dto.NotificationResponse;
import com.ecommerce.api.entity.User;
import com.ecommerce.api.exception.ResourceNotFoundException;
import com.ecommerce.api.repository.UserRepository;
import com.ecommerce.api.service.NotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notificaciones")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> findMine(Authentication authentication) {
        return ResponseEntity.ok(notificationService.findMine(resolveUserId(authentication.getName())));
    }

    @GetMapping("/no-leidas/cantidad")
    public ResponseEntity<Map<String, Long>> countUnread(Authentication authentication) {
        long count = notificationService.countUnread(resolveUserId(authentication.getName()));
        return ResponseEntity.ok(Map.of("cantidad", count));
    }

    @PatchMapping("/{id}/leer")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID id) {
        notificationService.markAsRead(id);
        return ResponseEntity.noContent().build();
    }

    private UUID resolveUserId(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", username));
        return user.getId();
    }
}
