package io.movetodata.build.library.models;


import com.fasterxml.jackson.annotation.JsonBackReference;
import io.movetodata.build.BobEnums.BuildStage;
import io.movetodata.build.BobEnums.FunnelStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "build_log_messages")
public class BuildLogMessages {

    private @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;
    private Date startedAt = new Date();

    @Enumerated(EnumType.STRING)
    private BuildStage stage;

    @Enumerated(EnumType.STRING)
    private FunnelStatus status;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(columnDefinition = "TEXT")
    private String debug;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "buildIdStarting")
    @JsonBackReference
    private BuildLog buildLogStarting;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "buildIdPreparing")
    @JsonBackReference
    private BuildLog buildLogPreparing;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "buildIdRunning")
    @JsonBackReference
    private BuildLog buildLogRunning;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "buildIdFinished")
    @JsonBackReference
    private BuildLog buildLogFinished;


}
