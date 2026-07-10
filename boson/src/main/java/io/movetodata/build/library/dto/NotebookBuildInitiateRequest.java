package io.movetodata.build.library.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class NotebookBuildInitiateRequest {
    private UUID buildId;
    private String branch;
    private UUID userId;

}
