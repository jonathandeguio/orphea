package io.orphea.julia.library.models;

import lombok.*;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GitCommit {

    private String email;
    private String username;
    private String message;
    private String id;
}
