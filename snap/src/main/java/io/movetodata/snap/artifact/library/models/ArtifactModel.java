package io.movetodata.snap.artifact.library.models;

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
@Table(name = "version")
public class ArtifactModel {
    public @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;
    public String name;

    public String resourceVersionId;
    public Date createdAt;
    public Date updatedAt;
    public UUID createdBy;
    public UUID updatedBy;

}