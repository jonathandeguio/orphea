package io.orphea.fractal.library.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Builder
public class BlameResultDTO {
    private String id;
    private String committer;
    private String author;
    private String message;
    private Integer line;
    private Date commitDate;
}
