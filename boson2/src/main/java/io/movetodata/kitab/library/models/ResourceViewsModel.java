package io.movetodata.kitab.library.models;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "kitab_resource_views")
public class ResourceViewsModel {

    public @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;

    public UUID resourceId;
    public String action;  // viewed , created, deleted
    @CreationTimestamp
    public Date viewedAt = new Date();
    public UUID viewedBy;

}
