package io.orphea.passport.library.service;

import io.orphea.passport.library.models.NotificationPreferences;
import io.orphea.passport.library.models.User;
import io.orphea.passport.library.models.UserPreferences;
import io.orphea.passport.library.repository.NotificationPreferencesRepository;
import io.orphea.platform.library.services.PlatformConfigService;
import io.orphea.sharedutils.Exceptions.BadRequestException;
import io.orphea.sharedutils.Exceptions.MaxLimitExceededException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RequiredArgsConstructor
@Service
public class UserHandlerService {
    private final PlatformConfigService platformConfigService;
    private final UserService userService;
    private final NotificationPreferencesRepository notificationPreferencesRepository;

    public User createUser(User user) {
        if(Boolean.TRUE.equals(platformConfigService.isUserLimitReached()))
            throw new MaxLimitExceededException("Maximum No. of Users Limit Reached.");
        if(Boolean.TRUE.equals(userService.isUsernameAlreadyAssigned(user.getUsername())))
            throw new BadRequestException("Username Already Assigned.");


        user.setPreferences(new UserPreferences());
        NotificationPreferences notificationPreferences = new NotificationPreferences();
        notificationPreferencesRepository.save(notificationPreferences);
        user.setNotificationPreferencesId(notificationPreferences.getId());

        return userService.saveUser(user);
    }
}
