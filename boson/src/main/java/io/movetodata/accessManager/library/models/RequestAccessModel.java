package io.movetodata.accessManager.library.models;

import io.movetodata.accessManager.library.enums.AccessRequestType;
import io.movetodata.passport.enums.AuthRole;
import lombok.*;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.List;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestAccessModel {
    private String title;
    private String description;
    private UUID requestTargetId;
    @Enumerated(EnumType.STRING)
    private AuthRole role;
    @Enumerated(EnumType.STRING)
    private AccessRequestType type;
    private List<UUID> requesters;
    private UUID requestTargetIds;
}
