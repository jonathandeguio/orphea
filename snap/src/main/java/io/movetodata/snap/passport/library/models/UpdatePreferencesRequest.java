package io.movetodata.snap.passport.library.models;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UpdatePreferencesRequest {

    UUID userId;

    private boolean autoFormatSQL;
    private boolean folderListView;
    private boolean CMDOpen;
    private boolean searchOpen;
    private boolean map;
    private Integer fontSize;
    private String language;
    private String mode;
    private String timestampFormat;


}
