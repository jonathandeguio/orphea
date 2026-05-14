package io.orphea.passport.library.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.persistence.*;
import java.util.UUID;
@Builder
@Entity
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "passport_notification_preferences")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class NotificationPreferences {
    public @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;

    private Boolean mention = true;
    private Boolean subscription = true;
    private Boolean accessManager = true;
}







