package io.bosler.passport.library.service;

import io.bosler.passport.library.models.NotificationPreferences;
import io.bosler.passport.library.repository.NotificationPreferencesRepository;
import io.bosler.passport.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationPreferencesService {
    private final NotificationPreferencesRepository notificationPreferencesRepository;
    private  final UserRepository userRepository;

    public NotificationPreferences getNotificationsPreferences(UUID userId) {
        UUID notificationPreferencesId = userRepository.getReferenceById(userId).getNotificationPreferencesId();
        if (notificationPreferencesId != null) {
            return notificationPreferencesRepository.getReferenceById(notificationPreferencesId);
        }
        return null;
    }
}
