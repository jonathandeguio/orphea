package io.bosler.docket.library.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Transactional
@Table(name = "docket_tag_category")
public class TagsCategory {

    public @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;

    private String name;
    private String description;
    private boolean enabled = true;

    @OneToMany(mappedBy = "tagsCategory", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Tags> tags;

    @CreationTimestamp
    private Date createdAt = new Date();
    private UUID createdBy;

    private UUID updatedBy;
    @LastModifiedDate
    private Date updatedAt;


}
