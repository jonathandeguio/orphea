package io.movetodata.fractal.library.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
@AllArgsConstructor
public class GitLog {
    private String id;
    private String username;
    private String email;
    private String body;
    private String message;
    private int commitTime;
}
