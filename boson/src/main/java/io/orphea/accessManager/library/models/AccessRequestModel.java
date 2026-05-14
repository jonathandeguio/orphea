package io.orphea.accessManager.library.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.orphea.accessManager.library.enums.AccessRequestStatus;
import io.orphea.accessManager.library.enums.AccessRequestType;
import io.orphea.passport.enums.AuthRole;
import lombok.*;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Table(name = "access_request")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class AccessRequestModel {
    private @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;

    private String title;
    private String description;
    private String requestTargetName;
    private UUID requestTargetId;

    @Enumerated(EnumType.STRING)
    private AccessRequestStatus status =  AccessRequestStatus.OPEN;

    @Enumerated(EnumType.STRING)
    private AccessRequestType type = AccessRequestType.PROJECT;
    @ElementCollection
    private List<UUID> requesters;
    @ElementCollection
    private List<UUID> assignees;

    @Enumerated(EnumType.STRING)
    private AuthRole role;

    private String closingRemarks;

    private Date createdAt;
    private Date updatedAt;
    private Date closedAt;
    private UUID createdBy;
    private UUID updatedBy;
    private UUID closedBy;
}
