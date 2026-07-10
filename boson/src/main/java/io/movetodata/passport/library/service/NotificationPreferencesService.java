package io.movetodata.passport.library.service;

import io.movetodata.passport.library.models.NotificationPreferences;
import io.movetodata.passport.library.repository.NotificationPreferencesRepository;
import io.movetodata.passport.library.repository.UserRepository;
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
