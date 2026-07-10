package io.movetodata.snap.notifications.library.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "kitab_notifications")
public class Notification {
    public @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;

    public UUID resourceId;
    public String status;

    @Column(columnDefinition = "TEXT")
    public String message;

    public Boolean isRead = false;
    public String type;
    public UUID influencer;
    public UUID subscriber;
    public Date timestamp;
}
