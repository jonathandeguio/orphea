package io.bosler.kepler.library.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "kitab_resource_version_details_changes_wrapper_changes")
public class VersionChangesModel {
    private @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;

    private String key;
    private boolean treat = true;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "changesWrapperId_FK")
    @JsonBackReference
    private ChangesWrapperModel changesWrapper;
}
