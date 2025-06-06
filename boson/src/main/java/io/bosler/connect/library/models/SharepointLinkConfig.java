package io.bosler.connect.library.models;

import io.bosler.kitab.library.enums.ResourceSubtype;
import lombok.*;

import javax.persistence.*;
import java.util.UUID;

@Getter
@Setter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sharepoint_link_config")
public class SharepointLinkConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String fileId;
    private String sheetName;
    @Enumerated(EnumType.STRING)
    private ResourceSubtype fileType;
}
