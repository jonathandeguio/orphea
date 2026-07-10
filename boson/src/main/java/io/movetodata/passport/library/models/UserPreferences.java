package io.movetodata.passport.library.models;

import io.movetodata.passport.enums.LayoutView;
import lombok.*;
import org.simpleframework.xml.Text;

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

    private Boolean CMDOpen = false;
    private Boolean searchOpen = false;
    private Boolean map = true;
    private Integer fontSize = 12;
    private String language = "auto";
    private String mode = "auto";
    private String timestampFormat = "Default";
    @Enumerated(EnumType.STRING)
    private LayoutView layoutView = LayoutView.COMPACT;
    private Boolean autoFormatSQL = true;
    private Boolean folderListView = true;
    private Boolean sidePanelOpen = true;
    private Boolean hideFiles = true;

    @Builder.Default
    @Column(columnDefinition = "TEXT")
    private String customTheme = "[]";
}

