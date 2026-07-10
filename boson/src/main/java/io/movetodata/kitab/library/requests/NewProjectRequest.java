package io.movetodata.kitab.library.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewProjectRequest {
    String name;
    String description;
    boolean groups = false;
    boolean folders = false;
    String userLanguage = "en";
}
