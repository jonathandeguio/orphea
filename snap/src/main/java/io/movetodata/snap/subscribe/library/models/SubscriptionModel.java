package io.movetodata.snap.subscribe.library.models;

import io.movetodata.snap.kitab.library.enums.ResourceType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "subscription")
public class SubscriptionModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public UUID id;
    public String name;

    public UUID jobId;
    public String sendTo;
    public String subject = "";
    public String body = "";
    public ResourceType resourceType;

    public UUID resourceId;

    public UUID tabId;

    // Schedule Information
    public String cronExpression;
    public String startTime;

    // Configuration
    public Boolean paused = true;
    public Boolean previewImage = false;
    public Boolean providePermission = false;


    @CreationTimestamp
    public Date createdAt;
    @LastModifiedDate
    public Date updatedAt;
    public UUID createdBy;
    public UUID updatedBy;

}
