package io.movetodata.kitab.library.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.movetodata.docket.library.models.Tags;
import io.movetodata.passport.library.models.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "kitab_dataset")
public class DatasetModel {

    public @Id
    UUID id;

    public String type;

//    @ElementCollection
//    @CollectionTable(name = "kitab_dataset_tags", joinColumns=@JoinColumn(name = "dataset_id"))
//    @JoinColumn(name = "dataset_id")
//    @OnDelete(action = OnDeleteAction.CASCADE)
//    @Cascade(value={org.hibernate.annotations.CascadeType.ALL})
    @ManyToMany(targetEntity = Tags.class)
    @JoinTable(
        joinColumns = { @JoinColumn(name = "dataset_id") },
        inverseJoinColumns = { @JoinColumn(name = "tag_id") }
    )
    public List<Tags> tags;

    @CreationTimestamp
    public Date createdAt = new Date();
    @LastModifiedDate
    public Date updatedAt = new Date();
    public UUID createdBy;
    public UUID updatedBy;

}
