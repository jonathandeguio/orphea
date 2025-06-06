package io.bosler.synchro.library.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
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
@Builder
@Table(name = "sync_specification")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SyncSpecification {

    public @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;
    public UUID datasetId;
    public String branch;
    public UUID sourceId;
    public boolean isDataMartSyncSpec = false;
    public UUID dataMartId;

    public String tableName;
    public boolean autoSyncOnBuild;
    @CreationTimestamp
    public Date createdAt = new Date();
    public UUID createdBy;
    public UUID updatedBy;
    @LastModifiedDate
    public Date updatedAt;

    @OneToMany()
    @JoinColumn(name = "sync_specification_id")
    private List<SyncIndex> syncIndexes;
}
