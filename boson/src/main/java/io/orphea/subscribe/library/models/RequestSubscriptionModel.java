package io.orphea.subscribe.library.models;

import io.orphea.kitab.library.enums.ResourceType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RequestSubscriptionModel {
    public UUID id;
    public String name;

    public UUID jobId;
    public String sendTo;
    public String subject = "";
    public String body = "";

    // Subscription Type
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

    public Date createdAt;
    public Date updatedAt;
    public UUID createdBy;
    public UUID updatedBy;


//    public String name;
//
//    public UUID jobId;
//
//    // Email Details
//    public String sendTo;
//    public String subject = "";
//    public String body = "";
//
//    // Subscription Type
//    public String resourceType;
//    public UUID resourceId;
//
//    // Schedule Information
//    public String cronExpression;
//    public String startTime;
//
//    // Configuration
//    public Boolean paused = true;
//    public Boolean previewImage = false;
//    public Boolean providePermission = false;
}
