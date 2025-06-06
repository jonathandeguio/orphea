package io.bosler.connect.library.requests;

import io.bosler.connect.library.models.RestAPIRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class WebhookDTO {
    private UUID id;
    private UUID parent;
    private String name;
    private String description;
    private UUID sourceId;
    private List<RestAPIRequest> requests;
}
