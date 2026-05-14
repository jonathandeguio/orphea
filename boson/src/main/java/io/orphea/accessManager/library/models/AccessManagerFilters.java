package io.orphea.accessManager.library.models;

import io.orphea.accessManager.library.enums.AccessRequestStatus;
import io.orphea.accessManager.library.enums.AccessRequestType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class AccessManagerFilters {
    private String searchText;

    @Enumerated(EnumType.STRING)
    private List<AccessRequestStatus> status;

    @Enumerated(EnumType.STRING)
    private AccessRequestType type;

    private Date rangeFrom;
    private Date rangeTo;

    private boolean showMyRequestsOnly;
    private List<UUID> requesters;
}
