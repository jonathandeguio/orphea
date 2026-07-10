package io.movetodata.docket.library.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.Date;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Transactional
@Table(name = "docket_tags")
public class Tags {


    public @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;


    public String name;
    public String description;
    public String color;

    @ManyToOne()
    @JoinColumn(name = "tags_category_id")
    @JsonBackReference
    @NotNull
    private TagsCategory tagsCategory;

    @CreationTimestamp
    public Date createdAt = new Date();
    public UUID createdBy;

    public UUID updatedBy;
    @LastModifiedDate
    public Date updatedAt;
}
