package io.movetodata.kitab.library.models;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.movetodata.docket.library.models.Tags;
import io.movetodata.sharedutils.MoveToDataUtils;
import io.movetodata.sharedutils.Serializers.ResourceSerializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Transactional
@JsonSerialize(using = ResourceSerializer.class)
@Table(name = "kitab_dataset")
public class DatasetModel implements MoveToDataUtils, IResource {

    public @Id
    UUID id;
    public UUID dsID;
    public String type;

    @ManyToMany(targetEntity = Tags.class)
    @JoinTable(
            joinColumns = {@JoinColumn(name = "dataset_id")},
            inverseJoinColumns = {@JoinColumn(name = "tag_id")}
    )
    public List<Tags> tags;

    @OneToMany(targetEntity = BranchModel.class, fetch = FetchType.EAGER)
    private Set<BranchModel> branches = new HashSet<>();
}
