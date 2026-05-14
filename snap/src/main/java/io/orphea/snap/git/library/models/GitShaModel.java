package io.orphea.snap.git.library.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "build_git_repository_branch_commit_details")
public class GitShaModel {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonIgnore
    private UUID id;

    private String sha;
    private String url;
    private String createdBy;
    private String message;
    private Date createdAt;

    @ManyToOne
    @JoinColumn(name = "build_git_repository_branch_details_id") // Updated to map to GitBranchModel
    @JsonBackReference
    private GitBranchModel branch;
}
