package io.movetodata.kepler.library.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

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
@Table(name = "kitab_resource_version_details")
public class ResourceVersionDetailsModel {
    private @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;
    private Long versionId;
    private String name;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "resourceId")
    @JsonIgnore
    private ResourceVersionsModel resourceVersionsModel;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "resourceVersionDetails")
    private List<ChangesWrapperModel> changesWrapper = new ArrayList<>();

    @CreationTimestamp
    private Date createdAt;
    private UUID createdBy;
}

