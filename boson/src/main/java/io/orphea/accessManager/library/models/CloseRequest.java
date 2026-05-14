package io.orphea.accessManager.library.models;

import io.orphea.accessManager.library.enums.AccessRequestStatus;
import io.orphea.accessManager.library.enums.AccessRequestType;
import io.orphea.passport.enums.AuthRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.List;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CloseRequest {
    private UUID requestId;
    private String closingRemarks;
    @Enumerated(EnumType.STRING)
    private AccessRequestStatus status;
}
