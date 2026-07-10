package io.movetodata.snap.git.library.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
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
@Table(name = "build_git_repository_details")
public class GitRepositoryModel {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String name;
    private String fullName;
    private Boolean isPrivate;
    private String description;
    private String cloneUrl;
    private String tagsUrl;
    private String commitsUrl;
    private Integer repoSize;
    private String repositoryLanguage;
    private String defaultBranch;

    private Date createdAt;
    private Date updatedAt;
    private Date pushedAt;

    @OneToMany(mappedBy = "repository", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<GitBranchModel> branches;

    @OneToMany(mappedBy = "repository", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<GitTagModel> tags;
}

