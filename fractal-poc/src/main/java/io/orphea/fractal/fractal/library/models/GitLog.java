package io.orphea.fractal.fractal.library.models;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class GitLog {

    private String id;
    private String username;
    private String email;
    private String body;
    private String message;
}
