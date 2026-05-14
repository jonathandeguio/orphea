package io.orphea.snap.git.library.repository;

import io.orphea.snap.git.library.models.GitBranchModel;
import io.orphea.snap.git.library.models.GitShaModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GitShaRepository extends JpaRepository<GitShaModel, UUID> {
    GitShaModel findBySha(String sha);
    List<GitShaModel> findByBranch(GitBranchModel branch);
}
