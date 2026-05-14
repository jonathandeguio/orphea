package io.orphea.notifications.library.services;

import io.orphea.notifications.library.models.Notification;
import io.orphea.sharedutils.models.PageSettings;
import io.orphea.notifications.library.repository.NotificationRepository;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Transactional
@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    @Autowired
    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Page<Notification> getNotificationPage(UUID userId, @NonNull PageSettings pageSetting) {
        Sort notificationSort = pageSetting.buildSort();
        Pageable notificationPage = PageRequest.of(pageSetting.getPage(), pageSetting.getElementPerPage(), notificationSort);
        return notificationRepository.findAllBySubscriberEquals(userId, notificationPage);
    }
}
