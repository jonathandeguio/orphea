package io.bosler.build.library.models;

import io.bosler.build.library.keys.PreviewSpecsKey;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "preview_specs")
@IdClass(PreviewSpecsKey.class)
public class PreviewSpecsModel {
    Integer rowLimit;
    @Id
    private UUID repositoryId;
    @Id
    private String branch;
}
