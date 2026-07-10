package io.movetodata.kepler.library.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "kitab_resource_version_details_changes_wrapper")
public class ChangesWrapperModel {
    private @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "changesWrapper")
    private List<VersionChangesModel> changes = new ArrayList<>();

    private String heading;
    private UUID userId;
    private Date entryTime;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "resourceVersionDetailsId_FK")
    @JsonBackReference
    private ResourceVersionDetailsModel resourceVersionDetails;
}
