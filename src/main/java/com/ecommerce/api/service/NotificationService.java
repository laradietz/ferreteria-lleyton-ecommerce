package com.ecommerce.api.service;

import com.ecommerce.api.dto.NotificationResponse;
import com.ecommerce.api.entity.Notification;
import com.ecommerce.api.entity.NotificationType;
import com.ecommerce.api.entity.User;
import com.ecommerce.api.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public List<NotificationResponse> findMine(UUID userId) {
        return notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toResponse).toList();
    }

    public long countUnread(UUID userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public void markAsRead(UUID id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    @Transactional
    public void send(User user, NotificationType type, String subject, String body) {
        Notification n = Notification.builder()
                .user(user).type(type).subject(subject).body(body).read(false).build();
        notificationRepository.save(n);
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(n.getId(), n.getType().name(), n.getSubject(),
                n.getBody(), n.getRead(), n.getCreatedAt());
    }
}
