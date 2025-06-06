package io.bosler.bob.library.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "bob_build_log")
public class BuildLog {
    public @Id
    UUID id;

    public UUID repository;
    public String branch;
    public String scriptPath;

    public String sparkApplicationId;
    public String trigger;

    public String status;

    @OneToMany(mappedBy = "buildLog")
    @JsonManagedReference
    private List<BuildStageLog> buildStageLogs;


    public UUID startedBy;
    public Date startedAt = new Date();
    public Date finishedAt;

}