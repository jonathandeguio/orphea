package io.movetodata.synchro.library.models;

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
@Table(name = "sync_index")
public class SyncIndex {
    public @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;
    @CreationTimestamp
    public Date createdAt = new Date();
    public UUID createdBy;
    public UUID updatedBy;
    @LastModifiedDate
    public Date updatedAt;

    @ElementCollection
    @Column(name = "column_name")
    @CollectionTable(name = "sync_index_columns", joinColumns = @JoinColumn(name = "owner_id"))
    private List<String> columns;
}
