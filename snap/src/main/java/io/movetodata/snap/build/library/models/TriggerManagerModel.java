package io.movetodata.snap.build.library.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.movetodata.snap.build.library.enums.BuildStatus;
import io.movetodata.snap.build.library.enums.BuildType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "build_trigger")
public class TriggerManagerModel {
    private @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;
    private String name;
    private String description;
    @Column(name = "branch")
    private String branch;
    private String repoName;
    private String repoUrl;
    private String latestTag;
    private String commitId;

    @Enumerated(EnumType.STRING)
    private BuildType buildType;

    private String configFileName;

    @Column(name = "harbor_project_name")
    private String harborProjectName;

    @Enumerated(EnumType.STRING)
    private BuildStatus buildStatus;

    private Date buildAt;
    private UUID buildBy;

    @OneToMany(mappedBy = "triggerManagerModel", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<TriggerArtifactModel> triggerArtifactModels;

    private Date createdAt;
    private Date updatedAt;

    private UUID createdBy;
    private UUID updatedBy;

}