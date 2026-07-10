package io.movetodata.snap.notifications.controllers;

import io.movetodata.snap.notifications.library.models.Notification;
import io.movetodata.snap.notifications.library.repository.NotificationRepository;
import io.movetodata.snap.notifications.library.services.NotificationService;
import io.movetodata.snap.passport.library.service.AuthzService;
import io.movetodata.snap.passport.library.service.UserService;
import io.movetodata.snap.sharedutils.DTO.PageToPageDTOMapper;
import io.movetodata.snap.sharedutils.models.PageSettings;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Notifications", description = "This is a notifications management service.")
public class NotificationsController {
    private final UserService userService;
    private final AuthzService authzService;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;
    private final PageToPageDTOMapper pageToPageDTOMapper;


    @Autowired
    SimpMessagingTemplate template;

    @Operation(summary = "Get All the notifications for particular subscriber")
    @GetMapping("/allNotifications")
    public ResponseEntity<Object> getAllNotifications(Principal principal, PageSettings pageSettings) {
        log.info(
                "Request for notification page received with data : " + pageSettings);
        UUID userId = userService.getUser(principal.getName()).getId();

        return ResponseEntity.ok().body(pageToPageDTOMapper.pageToPageDTO(
                notificationService.getNotificationPage(userId, pageSettings)));
    }

    @Operation(summary = "Delete the particular notification")
    @DeleteMapping("/{id}")
    @PreAuthorize("isSubscriberOrPlatformAdmin(#id)")
    public ResponseEntity<Object> deleteNotification(Principal principal, @PathVariable("id") UUID id) {
        if (!notificationRepository.existsById(id))
            return new ResponseEntity<>("No Notification with the id exist", HttpStatus.NOT_FOUND);
        Notification notification = notificationRepository.findById(id).orElseThrow();
        notificationRepository.delete(notification);
        return ResponseEntity.ok().body("Notification Deleted Successfully");
    }
    @Operation(summary = "Read a particular Notification")
    @PostMapping("/{id}/read")
    @PreAuthorize("isSubscriberOrPlatformAdmin(#id)")
    public ResponseEntity<Object> readNotification(Principal principal, @PathVariable("id") UUID id) {
        if (!notificationRepository.existsById(id))
            return new ResponseEntity<>("No Notification with the id exist", HttpStatus.NOT_FOUND);
        Notification notification = notificationRepository.findById(id).orElseThrow();
        notification.setIsRead(true);
        notificationRepository.save(notification);
        return ResponseEntity.ok().body("Notification Read Successfully");
    }

    @Operation(summary = "Read a particular Notification")
    @PostMapping("/readAll")
    public ResponseEntity<Object> readAllMyNotifications(Principal principal) {
        UUID userId = userService.getUser(principal.getName()).getId();
        Iterable<Notification> notifications = notificationRepository.findAllBySubscriber(userId);
        for(Notification notification : notifications){
            notification.setIsRead(true);
            notificationRepository.save(notification);
        }
        return ResponseEntity.ok().body("All Notification Read Successfully");
    }

}
