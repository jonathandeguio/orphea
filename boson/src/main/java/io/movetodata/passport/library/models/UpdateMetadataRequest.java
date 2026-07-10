package io.movetodata.passport.library.models;

import io.movetodata.passport.enums.LayoutView;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
public class UpdateMetadataRequest {

    UUID id;

    private String name;
    private String password;
    private String givenName;
    private String familyName;
    private String location;
    private String profileImage;

    private String email;

    private Boolean CMDOpen;
    private Boolean searchOpen;
    private Boolean map;
    private Boolean autoFormatSQL;
    private Boolean folderListView;
    private Boolean SidePanelOpen;

    @Enumerated(EnumType.STRING)
    private LayoutView layoutView;

    private Integer fontSize;
    private String language;
    private String mode;
    private String timestampFormat;
    private String customTheme;
    private Boolean isMfaSkipped;
    private Date mfaSkippedDate;

}
