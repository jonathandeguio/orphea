package io.movetodata.connect.library.requests;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
    public class AgentRequestDTO {
    private String name;
    private UUID parent;
    private String description;
    private boolean proxy = false;
    private String httpProxy;
    private String httpsProxy;
    private Date lastStatus;
}
