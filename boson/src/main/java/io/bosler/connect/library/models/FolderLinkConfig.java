package io.bosler.connect.library.models;

import lombok.*;

import javax.persistence.*;
import java.util.UUID;

@Getter
@Setter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "folder_link_config")
public class FolderLinkConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String subFolder;
}
