package io.movetodata.snap.build.library.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.movetodata.snap.build.library.enums.BuildStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "build_artifact")
@Transactional
public class TriggerArtifactModel {
    public @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;

    public String tag;
    public String branch;
    public String commitId;

    @Enumerated(EnumType.STRING)
    public BuildStatus buildStatus;

    @OrderBy("Desc")
    public Date startedAt;
    public Date finishedAt;

    public Date createdAt;
    public Date updatedAt;
    public UUID createdBy;
    public UUID updatedBy;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "build_trigger_id")
    @JsonBackReference
    private TriggerManagerModel triggerManagerModel;
}