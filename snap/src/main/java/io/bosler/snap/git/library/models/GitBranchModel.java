package io.bosler.snap.git.library.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "build_git_repository_branch_details")
public class GitBranchModel {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonIgnore
    private UUID id;

    private String branchName;

    @OneToMany(mappedBy = "branch", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference  // Change this to JsonManagedReference
    private List<GitShaModel> commits;  // This will allow commits to be serialized


    private Boolean isDefault = false;

    @ManyToOne
    @JoinColumn(name = "build_git_repository_details_id") // Foreign key in GitBranchModel table
    @JsonBackReference
    private GitRepositoryModel repository;
}
