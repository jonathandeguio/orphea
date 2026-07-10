package io.movetodata.snap.comments.library.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "kitab_comments")
public class CommentModel {
    public @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;

    public UUID resourceId;
    public String status;

    @ElementCollection
    @OneToMany(cascade = CascadeType.ALL)
    public List<CommentModel> replies;
    public UUID parent;


    @Column(columnDefinition = "TEXT")
    public String message;


    public Date createdAt;
    public Date updatedAt;
    public UUID createdBy;
    public UUID updatedBy;
}
