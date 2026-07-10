package io.movetodata.kitab.library.models;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Setter
@DynamicUpdate
@Table(name = "kitab_resource_views")
public class ResourceViewsModel {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public UUID id;

    private UUID resourceId;
    private String action;  // viewed , created, deleted
    @CreationTimestamp
    private Date viewedAt = new Date();
    public UUID viewedBy;
}
