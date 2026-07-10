package io.movetodata.snap.passport.library.models;

import io.movetodata.snap.passport.enums.LayoutView;
import lombok.*;

import javax.persistence.*;
import java.util.UUID;

@Builder
@Entity
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "passport_users_preferences")
public class UserPreferences {

    public @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;

    private Boolean CMDOpen;
    private Boolean searchOpen;
    private Boolean map;
    private Integer fontSize;
    private String language;
    private String mode;
    private String timestampFormat;
    @Enumerated(EnumType.STRING)
    private LayoutView layoutView;
    private Boolean autoFormatSQL;
    private Boolean folderListView;
    private Boolean sidePanelOpen;
    private Boolean hideFiles;

    public String getLanguage() {
        return language;
    }
}

