package io.orphea.snap.git.library.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "build_git_repository_tag_details")
public class GitTagModel {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonIgnore
    private UUID id;

    private String tagName;
    private String commitSha;
    private String repositoryUrl;
    private String createdBy;
    private Date createdAt;

    @ManyToOne
    @JoinColumn(name = "build_git_repository_details_id") // Updated to a consistent foreign key name
    @JsonBackReference
    private GitRepositoryModel repository;
}