package io.bosler.fractal.fractal.library.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GitCommit {

    private String email;
    private String username;
    private String message;
    private String id;
}
