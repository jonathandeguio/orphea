package io.bosler.bob.library.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.Date;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "bob_build_stage_log")
public class BuildStageLog {


    public @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;

    public UUID repository;
    public String branch;
    public UUID datasetId;
    public String branchId;
    public String commitId;
    public String scriptPath;

//    public String logMessages;  // TODO : this needs to be another model linked

    public String status;

    @ManyToOne
    @JoinColumn(name = "build_log_id")
    @JsonBackReference
    private BuildLog buildLog;

    public Date startedAt = new Date();
    public Date finishedAt;
    public UUID startedBy;
}
