package io.movetodata.snap.notifications.library.repository;

import io.movetodata.snap.notifications.library.models.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.UUID;

public interface NotificationRepository extends PagingAndSortingRepository<Notification, UUID> {

    Page<Notification> findAllBySubscriberEquals(UUID subscriber, Pageable pageable);
    Iterable<Notification> findAllBySubscriber(UUID userId);

}
