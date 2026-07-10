package io.movetodata.snap.kitab.library.models;

import io.movetodata.snap.build.library.enums.BuildStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SocketMessage {
    private String message;
    private BuildStatus buildStatus;
    private String imageName;
    private String branch;
    private UUID artifactId;
    private UUID triggerManagerId;
    private Date timestamp;

}
