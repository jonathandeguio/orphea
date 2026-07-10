package io.movetodata.synchro.library.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "synchro_postgres_sync_specifications")
public class PostgresSyncSpecification {

    public @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;
    public UUID datasetId;
    public String branch;
    public String tableName;
    public boolean enabled;

    public String syncStatus;

    public Date startedAt;
    public Date finishedAt;

    @ElementCollection(targetClass = String.class)
    public List<List<String>> indexNames;

    public UUID syncedBy;

    @CreationTimestamp
    public Date createdAt = new Date();
    public UUID createdBy;

    public UUID updatedBy;
    @LastModifiedDate
    public Date updatedAt;


}
