package io.orphea.comments.library.services;

import io.orphea.build.library.models.SocketMessage;
import io.orphea.comments.library.models.CommentModel;
import io.orphea.notifications.library.enums.NotificationType;
import io.orphea.notifications.library.models.Notification;
import io.orphea.notifications.library.repository.NotificationRepository;
import io.orphea.passport.library.models.NotificationPreferences;
import io.orphea.passport.library.models.User;
import io.orphea.passport.library.repository.NotificationPreferencesRepository;
import io.orphea.passport.library.repository.UserRepository;
import io.orphea.scheduler.library.services.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static io.orphea.sharedutils.language.labels.getLabel;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional
public class CommentService {
    private final NotificationPreferencesRepository notificationPreferencesRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final MailService mailService;

    @Autowired
    SimpMessagingTemplate template;

    public void checkMessageAndDoNotificationStuff(CommentModel comment) throws MessagingException {
        String message = comment.getMessage();
        String[] words = message.split(" ");
        for (String word : words) {
            if (word.charAt(0) == '@') {
                String userName = word.substring(1);
                Optional<User> user = userRepository.findByUsername(userName);
                if (user.isPresent()) {
                    User userDetails = user.get();
                    if (!userDetails.getId().equals(comment.getCreatedBy()) && !userDetails.getId().equals(comment.getUpdatedBy())) {
                        //setNotification
                        Notification notification = new Notification();
                        notification.setMessage(comment.getMessage());
                        notification.setInfluencer(comment.getCreatedBy());
                        notification.setSubscriber(userDetails.getId());
                        notification.setType(NotificationType.MENTION);
                        notification.setResourceId(comment.getResourceId());
                        notification.setTimestamp(new Date());

                        notificationRepository.save(notification);

                        //send socket message to the user
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("id", notification.getId());
                        jsonObject.put("message", comment.getMessage());
                        jsonObject.put("influencer", comment.getCreatedBy());
                        jsonObject.put("subscriber", userDetails.getId());
                        jsonObject.put("type", "mention");
                        jsonObject.put("resourceId", comment.getResourceId());

                        SocketMessage socketMessage = new SocketMessage();
                        socketMessage.setMessage(jsonObject.toString());
                        template.convertAndSend("/topic/notification/" + userDetails.getId(), socketMessage);
                        UUID notificationPreferencesId = userDetails.getNotificationPreferencesId();
                        if (notificationPreferencesId != null) {
                            NotificationPreferences notificationPreferences = notificationPreferencesRepository.getReferenceById(notificationPreferencesId);
                            if (!notificationPreferences.getMention())
                                continue;
                        }
                        //send Mail
                        String link = System.getenv("BASE_URL") + "/portal/home";
                        mailService.sendMail(userDetails.getEmail(), getLabel("mentionedNotification", userDetails.getPreferences().getLanguage()),
                                getLabel("mentionedInMessage", userDetails.getPreferences().getLanguage()) + ": " + message, userDetails.getName(), link);
                    }
                }
            }
        }
    }
}
